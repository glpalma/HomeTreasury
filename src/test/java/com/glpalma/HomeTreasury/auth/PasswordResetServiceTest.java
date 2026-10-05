package com.glpalma.HomeTreasury.auth;

import com.glpalma.HomeTreasury.user.AppUser;
import com.glpalma.HomeTreasury.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    private AppUserRepository users;
    private PasswordResetTokenRepository resetTokens;
    private PasswordEncoder passwordEncoder;
    private JavaMailSender mailSender;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        users = mock(AppUserRepository.class);
        resetTokens = mock(PasswordResetTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        mailSender = mock(JavaMailSender.class);
        service = new PasswordResetService(users, resetTokens, passwordEncoder, mailSender, "http://localhost:5173");
    }

    @Test
    void requestReset_unknownEmail_doesNothingSilently() {
        when(users.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.requestReset("ghost@example.com"));

        verify(resetTokens, never()).save(any());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void requestReset_knownEmail_savesTokenAndSendsLinkEmail() {
        AppUser user = new AppUser("alice@example.com", "hash");
        when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        service.requestReset("alice@example.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetTokens).save(tokenCaptor.capture());
        PasswordResetToken saved = tokenCaptor.getValue();
        assertEquals(user, saved.getUser());
        assertFalse(saved.isUsed());
        assertTrue(saved.getExpiresAt().isAfter(Instant.now()));
        assertFalse(saved.getToken().isBlank());

        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mailCaptor.capture());
        SimpleMailMessage sent = mailCaptor.getValue();
        assertArrayEquals(new String[]{"alice@example.com"}, sent.getTo());
        assertTrue(sent.getText().contains("http://localhost:5173/reset-password?token=" + saved.getToken()));
    }

    @Test
    void resetPassword_invalidOrExpiredToken_throws400() {
        when(resetTokens.findByTokenAndUsedFalseAndExpiresAtAfter(eq("bad-token"), any()))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.resetPassword("bad-token", "newPassword123"));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void resetPassword_validToken_updatesPasswordAndBurnsAllPendingTokensForUser() {
        AppUser user = new AppUser("alice@example.com", "oldHash");
        PasswordResetToken token = new PasswordResetToken(user, "good-token", Instant.now().plus(1, ChronoUnit.HOURS));
        PasswordResetToken otherPending = new PasswordResetToken(user, "other-token", Instant.now().plus(1, ChronoUnit.HOURS));

        when(resetTokens.findByTokenAndUsedFalseAndExpiresAtAfter(eq("good-token"), any()))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newHash");
        when(resetTokens.findAllByUserAndUsedFalse(user)).thenReturn(List.of(token, otherPending));

        service.resetPassword("good-token", "newPassword123");

        assertEquals("newHash", user.getPasswordHash());
        verify(users).save(user);
        assertTrue(token.isUsed());
        assertTrue(otherPending.isUsed());
    }
}
