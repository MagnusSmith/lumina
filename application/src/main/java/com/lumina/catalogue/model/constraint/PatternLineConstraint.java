package com.lumina.catalogue.model.constraint;

import static com.lumina.validation.ErrorCode.*;

import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Line;
import com.lumina.validation.ErrorBuilder;
import com.lumina.validation.Errors;
import com.lumina.validation.ValidationStageEnum;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.springframework.data.mongodb.core.mapping.Document;

@RecordBuilder
@Document
public record PatternLineConstraint(
    String name,
    String description,
    String pattern,
    boolean isRequired,
    @ValidationStageEnum ValidationStage stage)
    implements Constraint<Line.Pattern> {

  // Maximum pattern length to prevent complex regex patterns
  private static final int MAX_PATTERN_LENGTH = 500;
  // Pattern to detect potentially dangerous nested quantifiers
  private static final Pattern DANGEROUS_PATTERN =
      Pattern.compile("(.*[*+].*){3,}|(\\.\\*){3,}|([*+]{2,})|(.*(\\?.*){5,})");

  // Cache compiled patterns for performance
  private static final Map<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

  /**
   * Compact canonical constructor with ReDoS validation.
   *
   * @throws IllegalArgumentException if pattern is too long or potentially dangerous
   */
  public PatternLineConstraint {
    if (pattern != null) {
      if (pattern.length() > MAX_PATTERN_LENGTH) {
        throw new IllegalArgumentException(
            "Pattern exceeds maximum length of "
                + MAX_PATTERN_LENGTH
                + " characters: "
                + pattern.length());
      }
      if (DANGEROUS_PATTERN.matcher(pattern).find()) {
        throw new IllegalArgumentException(
            "Pattern contains potentially dangerous nested quantifiers or excessive complexity: "
                + pattern);
      }
    }
  }

  @Override
  public void validate(Line.Pattern line, Errors errors, ValidationStage stage) {
    if (stage().shouldValidateAt(stage)) {
      var value = line.value();

      try {
        // Use cached pattern or compile and cache if not present
        Pattern regExPattern =
            PATTERN_CACHE.computeIfAbsent(
                pattern,
                p -> {
                  try {
                    return Pattern.compile(p);
                  } catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException("Given regex is invalid: " + p, e);
                  }
                });

        if (!regExPattern.matcher(value).matches()) {
          errors.add(
              ErrorBuilder.builder()
                  .field(name)
                  .errorCode(INVALID_PATTERN)
                  .errorCodeArgs(new Object[] {value, pattern})
                  .rejectedValue(value)
                  .build());
        }
      } catch (IllegalArgumentException e) {
        throw e;
      }
    }
  }
}
