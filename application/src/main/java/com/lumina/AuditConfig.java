package com.lumina;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableMongoAuditing
public class AuditConfig {

  /**
   * Provides the current auditor (user) for audit fields.
   *
   * <p>When security is enabled, this returns the authenticated user's name. When security is
   * disabled (development mode), it returns "system".
   *
   * @return AuditorAware implementation
   */
  @Bean
  public AuditorAware<String> auditorProvider() {
    return () -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication == null
          || !authentication.isAuthenticated()
          || "anonymousUser".equals(authentication.getPrincipal())) {
        return Optional.of("system");
      }

      return Optional.of(authentication.getName());
    };
  }
}
