package com.lumina;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Request logging filter that sanitizes sensitive fields before logging.
 *
 * <p>This filter extends {@link CommonsRequestLoggingFilter} to redact sensitive information from
 * request payloads before they are logged. This prevents credentials, API keys, and other sensitive
 * data from being exposed in logs.
 */
public class SanitizingRequestLoggingFilter extends CommonsRequestLoggingFilter {

  private static final String REDACTED = "***REDACTED***";

  private final Set<String> sensitiveFields;
  private final Map<String, Pattern> jsonPatterns;
  private final Map<String, Pattern> queryPatterns;

  public SanitizingRequestLoggingFilter(Set<String> sensitiveFields) {
    this.sensitiveFields = sensitiveFields;

    // Pre-compile JSON patterns for better performance
    this.jsonPatterns =
        sensitiveFields.stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    field -> field,
                    field ->
                        Pattern.compile(
                            "\"" + Pattern.quote(field) + "\"\\s*:\\s*\"[^\"]*\"",
                            Pattern.CASE_INSENSITIVE)));

    // Pre-compile query parameter patterns for better performance
    this.queryPatterns =
        sensitiveFields.stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    field -> field,
                    field ->
                        Pattern.compile(
                            Pattern.quote(field) + "=[^&\\s]*", Pattern.CASE_INSENSITIVE)));
  }

  @Override
  protected void beforeRequest(HttpServletRequest request, String message) {
    // Sanitize message before logging
    String sanitized = sanitizeMessage(message);
    super.beforeRequest(request, sanitized);
  }

  @Override
  protected void afterRequest(HttpServletRequest request, String message) {
    // Sanitize message before logging
    String sanitized = sanitizeMessage(message);
    super.afterRequest(request, sanitized);
  }

  /**
   * Sanitizes the log message by replacing sensitive field values with REDACTED.
   *
   * @param message the original log message
   * @return the sanitized message
   */
  private String sanitizeMessage(String message) {
    if (message == null) {
      return null;
    }

    String sanitized = message;

    // Replace sensitive JSON field values using pre-compiled patterns
    for (String field : sensitiveFields) {
      // Match: "field":"value" or "field": "value"
      sanitized =
          jsonPatterns
              .get(field)
              .matcher(sanitized)
              .replaceAll("\"" + field + "\":\"" + REDACTED + "\"");

      // Also handle non-JSON query parameters: field=value
      sanitized = queryPatterns.get(field).matcher(sanitized).replaceAll(field + "=" + REDACTED);
    }

    return sanitized;
  }
}
