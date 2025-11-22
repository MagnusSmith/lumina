package com.lumina;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * Security filter chain for production environments with OAuth2 JWT authentication.
   *
   * <p>Requires the following properties to be set:
   *
   * <ul>
   *   <li>spring.security.oauth2.resourceserver.jwt.issuer-uri
   *   <li>spring.security.oauth2.resourceserver.jwt.jwk-set-uri (optional)
   * </ul>
   *
   * <p>This configuration is enabled when security is not explicitly disabled.
   */
  @Bean
  @ConditionalOnProperty(name = "lumina.security.enabled", havingValue = "true", matchIfMissing = true)
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            auth ->
                auth
                    // Allow health check endpoint for monitoring
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()
                    // Allow Swagger/OpenAPI endpoints for API documentation
                    .requestMatchers(
                        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/api-docs/**")
                    .permitAll()
                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
        .csrf(
            csrf ->
                csrf.ignoringRequestMatchers(
                    "/api/**")); // Disable CSRF for API endpoints (use JWT instead)

    return http.build();
  }

  /**
   * Development security filter chain that permits all requests.
   *
   * <p>This configuration is enabled when lumina.security.enabled=false is set in configuration.
   *
   * <p>WARNING: Only use this in development/testing environments!
   */
  @Bean
  @ConditionalOnProperty(name = "lumina.security.enabled", havingValue = "false")
  public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable());

    return http.build();
  }
}
