package com.lumina;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class LoggingConfig {

  /**
   * List of sensitive field names that should be redacted from logs.
   *
   * <p>Add any field names here that may contain sensitive information to ensure they are sanitized
   * before logging.
   */
  private static final Set<String> SENSITIVE_FIELDS =
      Set.of(
          "password",
          "privateKey",
          "apiKey",
          "secret",
          "token",
          "authorization",
          "cupsTrust",
          "lnsTrust",
          "pemCert");

  @Bean
  public SanitizingRequestLoggingFilter requestLoggingFilter() {
    SanitizingRequestLoggingFilter loggingFilter =
        new SanitizingRequestLoggingFilter(SENSITIVE_FIELDS);
    loggingFilter.setIncludeClientInfo(true);
    loggingFilter.setIncludeQueryString(true);
    loggingFilter.setIncludePayload(true);
    loggingFilter.setMaxPayloadLength(2000); // Reduced from 10000 to limit log size
    loggingFilter.setIncludeHeaders(false);
    loggingFilter.setAfterMessagePrefix("REQUEST DATA : ");
    return loggingFilter;
  }

  @Bean
  public Filter responseLoggingFilter() {
    return new Filter() {
      @Override
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();
        chain.doFilter(request, response);
        long duration = System.currentTimeMillis() - startTime;

        log.info(
            "RESPONSE DATA : {} {} - Status: {} - Duration: {}ms",
            httpRequest.getMethod(),
            httpRequest.getRequestURI(),
            httpResponse.getStatus(),
            duration);
      }
    };
  }
}
