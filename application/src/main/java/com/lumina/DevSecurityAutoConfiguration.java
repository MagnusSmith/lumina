package com.lumina;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Conditionally disables OAuth2 resource server autoconfiguration when security is disabled.
 *
 * <p>This prevents Spring Boot from trying to configure JWT authentication when
 * lumina.security.enabled=false, which would otherwise fail due to missing OAuth2 properties.
 */
@Configuration
@ConditionalOnProperty(name = "lumina.security.enabled", havingValue = "false")
@EnableAutoConfiguration(exclude = {OAuth2ResourceServerAutoConfiguration.class})
public class DevSecurityAutoConfiguration {
  // This class intentionally left empty
  // Its purpose is to exclude OAuth2 autoconfiguration in development mode
}
