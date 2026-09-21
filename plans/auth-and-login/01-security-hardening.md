# Security hardening

## What it does

Locks down the API surface: adds the Bean Validation annotation processor, wires a CORS policy for `http://localhost:5173`, enables method-level `@PreAuthorize` checks, and moves the `permitAll` matcher to the new `/api/auth/login` path.

## Why

Three independent gaps block every later slice:

1. **No `spring-boot-starter-validation`** — `@NotBlank` and `@Size` are present at compile time (they're in the API jar) but the validation interceptor is not on the classpath, so `@Valid` silently passes through invalid inputs.
2. **No CORS** — the SPA at `http://localhost:5173` is blocked by the browser before any request reaches the server.
3. **`@EnableMethodSecurity` is absent** — `@PreAuthorize("hasRole('OWNER')")` annotations on any controller method are parsed and then ignored at runtime; they give a false sense of authorization.

`CorsProperties` is a `@ConfigurationProperties` record rather than hard-coded values so the allowed origin can differ between environments without touching `SecurityConfig`.

## Data flow

This slice is pure infrastructure — no new HTTP request path. The visible change at the wire level is:

- **Before:** `OPTIONS /api/me` from `http://localhost:5173` → browser blocks it (no CORS headers returned).
- **After:** `OPTIONS /api/me` → 200 with `Access-Control-Allow-Origin: http://localhost:5173`.

## Today → after

**Today** in [`SecurityConfig.java`](../../src/main/java/com/glpalma/HomeTreasury/config/SecurityConfig.java): no `@EnableMethodSecurity`, no `.cors(...)`, `permitAll` on `/auth/login` (old path, no `/api` prefix).

**Today** in [`HomeTreasuryApplication.java`](../../src/main/java/com/glpalma/HomeTreasury/HomeTreasuryApplication.java): `@EnableConfigurationProperties({PluggyProperties.class, HomeProperties.class, OwnerProperties.class, JwtProperties.class})` — `CorsProperties` not registered.

**Today** in [`pom.xml`](../../pom.xml): `spring-boot-starter-validation` absent; `spring-boot-starter-web` is at lines 33–35.

**After:** All three gaps closed; `app.cors.allowed-origins` added to both properties files.

## Exact changes

Files ordered: properties files → POM → new config record → main class → security config.

- [ ] **Edit** [`src/main/resources/application.properties`](../../src/main/resources/application.properties) — add CORS origin property

  ```properties
  spring.application.name=HomeTreasury
  pluggy.client-id=${PLUGGY_CLIENT_ID}
  pluggy.client-secret=${PLUGGY_CLIENT_SECRET}
  pluggy.item-id=${PLUGGY_ITEM_ID}

  spring.datasource.url=${DB_URL}
  spring.datasource.username=${DB_USER}
  spring.datasource.password=${DB_PASSWORD}
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.show-sql=true
  home.name=${HOME_NAME}
  home.ideal-balance=${HOME_IDEAL_BALANCE}

  owner.email=${OWNER_EMAIL}
  owner.password=${OWNER_PASSWORD}

  jwt.secret=${JWT_SECRET}
  jwt.expiration-ms=${JWT_EXPIRATION_MS}

  app.cors.allowed-origins=http://localhost:5173
  ```

- [ ] **Edit** [`src/test/resources/application.properties`](../../src/test/resources/application.properties) — add CORS origin so the test context loads `CorsProperties` without error

  ```properties
  spring.datasource.url=jdbc:h2:mem:testdb
  spring.datasource.driver-class-name=org.h2.Driver
  spring.datasource.username=sa
  spring.datasource.password=
  spring.jpa.hibernate.ddl-auto=create-drop
  home.name=TestHome
  home.ideal-balance=100
  pluggy.client-id=test-client-id
  pluggy.client-secret=test-client-secret
  pluggy.item-id=test-item-id

  owner.email=owner@test.local
  owner.password=test-password

  jwt.secret=test-jwt-secret-must-be-at-least-32-chars-long
  jwt.expiration-ms=3600000

  app.cors.allowed-origins=http://localhost:5173
  ```

- [ ] **Edit** [`pom.xml`](../../pom.xml) — add `spring-boot-starter-validation` immediately after `spring-boot-starter-web`

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  	<modelVersion>4.0.0</modelVersion>
  	<parent>
  		<groupId>org.springframework.boot</groupId>
  		<artifactId>spring-boot-starter-parent</artifactId>
  		<version>4.1.0</version>
  		<relativePath/> <!-- lookup parent from repository -->
  	</parent>
  	<groupId>com.glpalma</groupId>
  	<artifactId>HomeTreasury</artifactId>
  	<version>0.0.1-SNAPSHOT</version>
  	<name/>
  	<description/>
  	<url/>
  	<licenses>
  		<license/>
  	</licenses>
  	<developers>
  		<developer/>
  	</developers>
  	<scm>
  		<connection/>
  		<developerConnection/>
  		<tag/>
  		<url/>
  	</scm>
  	<properties>
  		<java.version>21</java.version>
  	</properties>
  	<dependencies>
  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-web</artifactId>
  		</dependency>

  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-validation</artifactId>
  		</dependency>

  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-test</artifactId>
  			<scope>test</scope>
  		</dependency>

  		<dependency>
  			<groupId>me.paulschwarz</groupId>
  			<artifactId>springboot4-dotenv</artifactId>
  			<version>5.1.0</version>
  		</dependency>

  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-data-jpa</artifactId>
  		</dependency>
  		
  		<dependency>
  			<groupId>org.postgresql</groupId>
  			<artifactId>postgresql</artifactId>
  			<scope>runtime</scope>
  		</dependency>
  		
  		<dependency>
  			<groupId>com.h2database</groupId>
  			<artifactId>h2</artifactId>
  			<scope>test</scope>
  		</dependency>

  		<dependency>
  			<groupId>org.springframework.security</groupId>
  			<artifactId>spring-security-crypto</artifactId>
  		</dependency>

  		<dependency>
  			<groupId>org.springframework.boot</groupId>
  			<artifactId>spring-boot-starter-security</artifactId>
  		</dependency>

  		<dependency>
  			<groupId>io.jsonwebtoken</groupId>
  			<artifactId>jjwt-api</artifactId>
  			<version>0.12.6</version>
  		</dependency>

  		<dependency>
  			<groupId>io.jsonwebtoken</groupId>
  			<artifactId>jjwt-impl</artifactId>
  			<version>0.12.6</version>
  			<scope>runtime</scope>
  		</dependency>

  		<dependency>
  			<groupId>io.jsonwebtoken</groupId>
  			<artifactId>jjwt-jackson</artifactId>
  			<version>0.12.6</version>
  			<scope>runtime</scope>
  		</dependency>
  	</dependencies>

  	<build>
  		<plugins>
  			<plugin>
  				<groupId>org.springframework.boot</groupId>
  				<artifactId>spring-boot-maven-plugin</artifactId>
  			</plugin>
  		</plugins>
  	</build>

  </project>
  ```

- [ ] **New** [`src/main/java/com/glpalma/HomeTreasury/config/CorsProperties.java`](../../src/main/java/com/glpalma/HomeTreasury/config/CorsProperties.java) — binds `app.cors.allowed-origins` from properties into a typed list

  ```java
  package com.glpalma.HomeTreasury.config;

  import org.springframework.boot.context.properties.ConfigurationProperties;

  import java.util.List;

  @ConfigurationProperties(prefix = "app.cors")
  public record CorsProperties(List<String> allowedOrigins) {
  }
  ```

- [ ] **Edit** [`src/main/java/com/glpalma/HomeTreasury/HomeTreasuryApplication.java`](../../src/main/java/com/glpalma/HomeTreasury/HomeTreasuryApplication.java) — register `CorsProperties` in `@EnableConfigurationProperties`

  ```java
  package com.glpalma.HomeTreasury;

  import com.glpalma.HomeTreasury.config.CorsProperties;
  import com.glpalma.HomeTreasury.config.HomeProperties;
  import com.glpalma.HomeTreasury.config.JwtProperties;
  import com.glpalma.HomeTreasury.config.OwnerProperties;
  import com.glpalma.HomeTreasury.config.PluggyProperties;
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.boot.context.properties.EnableConfigurationProperties;

  @SpringBootApplication
  @EnableConfigurationProperties({
          PluggyProperties.class,
          HomeProperties.class,
          OwnerProperties.class,
          JwtProperties.class,
          CorsProperties.class
  })
  public class HomeTreasuryApplication {

  	public static void main(String[] args) {
  		SpringApplication.run(HomeTreasuryApplication.class, args);
  	}

  }
  ```

- [ ] **Edit** [`src/main/java/com/glpalma/HomeTreasury/config/SecurityConfig.java`](../../src/main/java/com/glpalma/HomeTreasury/config/SecurityConfig.java) — add `@EnableMethodSecurity`, `.cors(Customizer.withDefaults())`, `CorsConfigurationSource` bean; update `permitAll` path to `/api/auth/login`

  ```java
  package com.glpalma.HomeTreasury.config;

  import com.glpalma.HomeTreasury.auth.JwtAuthFilter;
  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.http.HttpStatus;
  import org.springframework.security.authentication.AuthenticationManager;
  import org.springframework.security.config.Customizer;
  import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
  import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.config.http.SessionCreationPolicy;
  import org.springframework.security.web.SecurityFilterChain;
  import org.springframework.security.web.authentication.HttpStatusEntryPoint;
  import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
  import org.springframework.web.cors.CorsConfiguration;
  import org.springframework.web.cors.CorsConfigurationSource;
  import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

  import java.util.List;

  @Configuration
  @EnableMethodSecurity
  public class SecurityConfig {

      @Bean
      AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
          return config.getAuthenticationManager();
      }

      @Bean
      SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
          http
                  .csrf(csrf -> csrf.disable())
                  .cors(Customizer.withDefaults())
                  .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                  .authorizeHttpRequests(auth -> auth
                          .requestMatchers("/api/auth/login").permitAll()
                          .anyRequest().authenticated()
                  )
                  .exceptionHandling(ex -> ex
                          .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                  )
                  .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
          return http.build();
      }

      @Bean
      CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
          CorsConfiguration config = new CorsConfiguration();
          config.setAllowedOrigins(corsProperties.allowedOrigins());
          config.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
          config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
          UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
          source.registerCorsConfiguration("/api/**", config);
          return source;
      }
  }
  ```

## Verify

1. Run: `./mvnw spring-boot:run` (requires a `.env` with real DB + JWT credentials, or use the test profile)
2. Expect: application starts with no `BeanCreationException` and logs `Tomcat started on port 8080`.
3. Send a CORS preflight (replace with real running server):
   ```
   curl -i -X OPTIONS http://localhost:8080/api/me \
     -H "Origin: http://localhost:5173" \
     -H "Access-Control-Request-Method: GET"
   ```
4. Expect response headers include:
   ```
   Access-Control-Allow-Origin: http://localhost:5173
   Access-Control-Allow-Methods: GET,POST,PUT,OPTIONS
   ```
5. Send the old login path (should no longer be `permitAll`):
   ```
   curl -i -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"owner@example.com","password":"secret"}'
   ```
6. Expect: `403` or `404` (not `200` — old path is no longer matched).

## Out of scope

- Changing any other CORS path besides `/api/**`.
- Adding `allowCredentials` (not needed for Bearer-token flows).
- Touching any endpoint mapping — that's slice 02.
- Creating `CorsProperties` in the `auth` or `web` package — it belongs in `config` alongside the other `*Properties` records.
