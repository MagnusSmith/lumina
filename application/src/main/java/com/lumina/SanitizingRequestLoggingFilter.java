package com.lumina;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.regex.Pattern;
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
  private final Pattern sensitiveFieldPattern;

  public SanitizingRequestLoggingFilter(Set<String> sensitiveFields) {
    this.sensitiveFields = sensitiveFields;

    // Build regex pattern to match sensitive field names in JSON
    // Matches patterns like: "password":"value" or "apiKey": "value"
    String fieldPattern =
        String.join(
            "|",
            sensitiveFields.stream()
                .map(field -> "\"" + field + "\"\\s*:\\s*\"[^\"]*\"")
                .toList());
    this.sensitiveFieldPattern = Pattern.compile(fieldPattern);
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

    // Replace sensitive JSON field values
    for (String field : sensitiveFields) {
      // Match: "field":"value" or "field": "value"
      Pattern pattern =
          Pattern.compile("\"" + field + "\"\\s*:\\s*\"[^\"]*\"", Pattern.CASE_INSENSITIVE);
      sanitized = pattern.matcher(sanitized).replaceAll("\"" + field + "\":\"" + REDACTED + "\"");

      // Also handle non-JSON query parameters: field=value
      Pattern queryPattern =
          Pattern.compile(field + "=[^&\\s]*", Pattern.CASE_INSENSITIVE);
      sanitized = queryPattern.matcher(sanitized).replaceAll(field + "=" + REDACTED);
    }

    return sanitized;
  }
}
