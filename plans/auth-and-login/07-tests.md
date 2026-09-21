# Tests

## What it does

Provides automated test coverage for the login endpoint and the `/api/me` endpoints so regressions surface in CI before they reach the browser.

## Why

`AuthControllerTest` uses `@WebMvcTest` — it loads only the web layer (controller, security filter chain, exception handler) with `@MockBean` collaborators, which makes it fast and avoids needing a database. This is the right scope because what we're testing is the HTTP contract (status codes, request validation, response shape), not the repositories.

`MeControllerTest` uses `@SpringBootTest` with the H2 in-memory database because `CurrentMembershipService` walks three JPA entities (`AppUser` → `HomeMembership` → `Home`) — mocking all of that out would mirror the implementation rather than test it. H2 + `@SpringBootTest` gives a real Spring Security filter chain and real JPA, at the cost of a slower test. `PluggyClient` is `@MockBean`-ed out because it calls an external HTTP service.

## Data flow

No new production code. Tests exercise the request paths already described in slices 02 and 05–06.

## Exact changes

Files ordered: `AuthControllerTest` first (covers slice 02, simpler setup) → `MeControllerTest` (covers slices 05–06, full context).

- [ ] **New** [`src/test/java/com/glpalma/HomeTreasury/auth/AuthControllerTest.java`](../../src/test/java/com/glpalma/HomeTreasury/auth/AuthControllerTest.java)

  ```java
  package com.glpalma.HomeTreasury.auth;

  import com.fasterxml.jackson.databind.ObjectMapper;
  import com.glpalma.HomeTreasury.user.AppUser;
  import com.glpalma.HomeTreasury.user.AppUserRepository;
  import com.glpalma.HomeTreasury.user.HomeMembership;
  import com.glpalma.HomeTreasury.user.HomeMembershipRepository;
  import com.glpalma.HomeTreasury.user.Role;
  import com.glpalma.HomeTreasury.home.Home;
  import org.junit.jupiter.api.Test;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
  import org.springframework.http.MediaType;
  import org.springframework.security.authentication.AuthenticationManager;
  import org.springframework.security.authentication.BadCredentialsException;
  import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
  import org.springframework.test.context.bean.override.mockito.MockitoBean;
  import org.springframework.test.web.servlet.MockMvc;

  import java.util.Optional;

  import static org.mockito.ArgumentMatchers.any;
  import static org.mockito.Mockito.when;
  import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
  import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

  @WebMvcTest(AuthController.class)
  class AuthControllerTest {

      @Autowired
      MockMvc mockMvc;

      @Autowired
      ObjectMapper objectMapper;

      @MockitoBean
      AuthenticationManager authenticationManager;

      @MockitoBean
      AppUserRepository users;

      @MockitoBean
      HomeMembershipRepository memberships;

      @MockitoBean
      JwtService jwtService;

      @Test
      void login_validCredentials_returns200WithTokenEmailRole() throws Exception {
          var user = new AppUser("owner@example.com", "hash");
          var home = new Home("My Home");
          var membership = new HomeMembership(user, home, Role.OWNER);

          when(authenticationManager.authenticate(any())).thenReturn(
                  new UsernamePasswordAuthenticationToken("owner@example.com", null)
          );
          when(users.findByEmail("owner@example.com")).thenReturn(Optional.of(user));
          when(memberships.findByUser(user)).thenReturn(Optional.of(membership));
          when(jwtService.createToken("owner@example.com", "OWNER")).thenReturn("signed-token");

          mockMvc.perform(post("/api/auth/login")
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(objectMapper.writeValueAsString(
                                  new LoginRequest("owner@example.com", "secret"))))
                  .andExpect(status().isOk())
                  .andExpect(jsonPath("$.token").value("signed-token"))
                  .andExpect(jsonPath("$.email").value("owner@example.com"))
                  .andExpect(jsonPath("$.role").value("OWNER"));
      }

      @Test
      void login_blankEmail_returns400WithErrorsArray() throws Exception {
          mockMvc.perform(post("/api/auth/login")
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(objectMapper.writeValueAsString(
                                  new LoginRequest("", "secret"))))
                  .andExpect(status().isBadRequest())
                  .andExpect(jsonPath("$.errors").isArray());
      }

      @Test
      void login_blankPassword_returns400WithErrorsArray() throws Exception {
          mockMvc.perform(post("/api/auth/login")
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(objectMapper.writeValueAsString(
                                  new LoginRequest("owner@example.com", ""))))
                  .andExpect(status().isBadRequest())
                  .andExpect(jsonPath("$.errors").isArray());
      }

      @Test
      void login_badCredentials_returns401() throws Exception {
          when(authenticationManager.authenticate(any()))
                  .thenThrow(new BadCredentialsException("bad"));

          mockMvc.perform(post("/api/auth/login")
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(objectMapper.writeValueAsString(
                                  new LoginRequest("owner@example.com", "wrong"))))
                  .andExpect(status().isUnauthorized());
      }

      @Test
      void login_oldPath_returns404() throws Exception {
          mockMvc.perform(post("/auth/login")
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(objectMapper.writeValueAsString(
                                  new LoginRequest("owner@example.com", "secret"))))
                  .andExpect(status().isNotFound());
      }
  }
  ```

- [ ] **New** [`src/test/java/com/glpalma/HomeTreasury/user/MeControllerTest.java`](../../src/test/java/com/glpalma/HomeTreasury/user/MeControllerTest.java)

  ```java
  package com.glpalma.HomeTreasury.user;

  import com.fasterxml.jackson.databind.ObjectMapper;
  import com.glpalma.HomeTreasury.auth.JwtService;
  import com.glpalma.HomeTreasury.pluggy.PluggyClient;
  import org.junit.jupiter.api.BeforeEach;
  import org.junit.jupiter.api.Test;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
  import org.springframework.boot.test.context.SpringBootTest;
  import org.springframework.http.MediaType;
  import org.springframework.security.crypto.password.PasswordEncoder;
  import org.springframework.test.context.bean.override.mockito.MockitoBean;
  import org.springframework.test.web.servlet.MockMvc;

  import java.util.Map;

  import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
  import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

  @SpringBootTest
  @AutoConfigureMockMvc
  class MeControllerTest {

      @Autowired
      MockMvc mockMvc;

      @Autowired
      ObjectMapper objectMapper;

      @Autowired
      JwtService jwtService;

      @Autowired
      AppUserRepository users;

      @Autowired
      HomeMembershipRepository memberships;

      @Autowired
      PasswordEncoder passwordEncoder;

      @MockitoBean
      PluggyClient pluggyClient;

      private String ownerToken;

      @BeforeEach
      void setUp() {
          // OwnerInitializer seeds owner@test.local / test-password via application.properties
          ownerToken = jwtService.createToken("owner@test.local", "OWNER");
      }

      @Test
      void me_noToken_returns401() throws Exception {
          mockMvc.perform(get("/api/me"))
                  .andExpect(status().isUnauthorized());
      }

      @Test
      void me_validToken_returns200WithEmailRoleHome() throws Exception {
          mockMvc.perform(get("/api/me")
                          .header("Authorization", "Bearer " + ownerToken))
                  .andExpect(status().isOk())
                  .andExpect(jsonPath("$.email").value("owner@test.local"))
                  .andExpect(jsonPath("$.role").value("OWNER"))
                  .andExpect(jsonPath("$.home.name").value("TestHome"));
      }

      @Test
      void changePassword_noToken_returns401() throws Exception {
          mockMvc.perform(put("/api/me/password")
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(objectMapper.writeValueAsString(
                                  Map.of("currentPassword", "test-password", "newPassword", "newpassword123"))))
                  .andExpect(status().isUnauthorized());
      }

      @Test
      void changePassword_wrongCurrentPassword_returns401() throws Exception {
          mockMvc.perform(put("/api/me/password")
                          .header("Authorization", "Bearer " + ownerToken)
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(objectMapper.writeValueAsString(
                                  Map.of("currentPassword", "wrong", "newPassword", "newpassword123"))))
                  .andExpect(status().isUnauthorized());
      }

      @Test
      void changePassword_newPasswordTooShort_returns400() throws Exception {
          mockMvc.perform(put("/api/me/password")
                          .header("Authorization", "Bearer " + ownerToken)
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(objectMapper.writeValueAsString(
                                  Map.of("currentPassword", "test-password", "newPassword", "short"))))
                  .andExpect(status().isBadRequest())
                  .andExpect(jsonPath("$.errors").isArray());
      }

      @Test
      void changePassword_validRequest_returns204AndUpdatesPassword() throws Exception {
          mockMvc.perform(put("/api/me/password")
                          .header("Authorization", "Bearer " + ownerToken)
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(objectMapper.writeValueAsString(
                                  Map.of("currentPassword", "test-password", "newPassword", "newpassword123"))))
                  .andExpect(status().isNoContent());

          // Verify DB was updated
          AppUser owner = users.findByEmail("owner@test.local").orElseThrow();
          assert passwordEncoder.matches("newpassword123", owner.getPasswordHash());

          // Restore original password so other tests are not affected
          owner.setPasswordHash(passwordEncoder.encode("test-password"));
          users.save(owner);
      }

      @Test
      void corsPreflight_fromAllowedOrigin_returns200() throws Exception {
          mockMvc.perform(options("/api/me")
                          .header("Origin", "http://localhost:5173")
                          .header("Access-Control-Request-Method", "GET"))
                  .andExpect(status().isOk())
                  .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
      }
  }
  ```

## Verify

1. Run: `./mvnw test`
2. Expect: `BUILD SUCCESS` with all tests in `AuthControllerTest` and `MeControllerTest` passing and no `BeanCreationException`.
3. To run only these two test classes:
   ```
   ./mvnw test -pl . -Dtest="AuthControllerTest,MeControllerTest" -Dsurefire.failIfNoSpecifiedTests=false
   ```
4. Expect output similar to:
   ```
   Tests run: 5, Failures: 0, Errors: 0, Skipped: 0  -- AuthControllerTest
   Tests run: 6, Failures: 0, Errors: 0, Skipped: 0  -- MeControllerTest
   BUILD SUCCESS
   ```

## Out of scope

- Integration tests against a real PostgreSQL instance — H2 is sufficient for this contract.
- Load or performance testing.
- Testing `CurrentMembershipService` in isolation — it is covered indirectly by `MeControllerTest`.
- Testing `ApiExceptionHandler`'s 404 and 502 paths — those depend on treasury slices not yet implemented.
