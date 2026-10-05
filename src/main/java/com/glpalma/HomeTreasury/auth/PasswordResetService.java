package com.glpalma.HomeTreasury.auth;

import com.glpalma.HomeTreasury.user.AppUser;
import com.glpalma.HomeTreasury.user.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private final AppUserRepository users;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String frontendUrl;

    public PasswordResetService(
            AppUserRepository users,
            PasswordResetTokenRepository resetTokens,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.users = users;
        this.resetTokens = resetTokens;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
    }

    // ponytail: no per-email cooldown, so repeated requests can flood a mailbox / the SMTP
    // relay's rate limit. Upgrade path: a per-email cooldown or a request-rate filter (e.g. Bucket4j).
    @Transactional
    public void requestReset(String email) {
        users.findByEmail(email).ifPresent(user -> {
            String token = generateToken();
            resetTokens.save(new PasswordResetToken(user, token, Instant.now().plus(1, ChronoUnit.HOURS)));
            sendResetEmail(user.getEmail(), token);
        });
        // Intentionally silent either way — the caller can't tell whether the email was registered.
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = resetTokens
                .findByTokenAndUsedFalseAndExpiresAtAfter(token, Instant.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        AppUser user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        users.save(user);

        // Burn every other pending token for this user too, so an older unused link can't be replayed later.
        resetTokens.findAllByUserAndUsedFalse(user).forEach(PasswordResetToken::markUsed);
    }

    private void sendResetEmail(String email, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset your HomeTreasury password");
        message.setText(
                "Click the link below to reset your password:\n\n" + link
                        + "\n\nThis link expires in 1 hour. If you didn't request this, ignore this email.");
        mailSender.send(message);
    }

    private static String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
