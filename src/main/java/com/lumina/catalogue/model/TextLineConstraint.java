package com.lumina.catalogue.model;

import static com.lumina.validation.ErrorCode.*;

import com.lumina.meter.model.Line;
import com.lumina.validation.EnumNamePattern;
import com.lumina.validation.ErrorBuilder;
import com.lumina.validation.Errors;
import io.micrometer.common.util.StringUtils;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Objects;

@RecordBuilder
public record TextLineConstraint(
    String name,
    String description,
    Integer minLength,
    Integer maxLength,
    boolean isRequired,
    @EnumNamePattern(regexp = "ZERO|ONE|TWO|THREE")
    ValidationStage stage)
    implements Constraint<Line.Text> {

  public void validate(Line.Text line, Errors errors, ValidationStage stage) {
    if (stage().shouldValidateAt(stage)) {
      var value = line.value();
      if (value instanceof String s) {

        if (StringUtils.isBlank(s)) {
          errors.rejectValue(name, REQUIRED);
        } else {
          if (Objects.nonNull(minLength) && minLength > s.length()) {
            errors.add(
                ErrorBuilder.builder()
                    .field(name)
                    .errorCode(MIN_LENGTH)
                    .errorCodeArgs(new Object[] {value, minLength})
                    .rejectedValue(value)
                    .build());
          } else if (Objects.nonNull(maxLength) && maxLength < s.length()) {
            errors.add(
                ErrorBuilder.builder()
                    .field(name)
                    .errorCode(MAX_LENGTH)
                    .errorCodeArgs(new Object[] {value, maxLength})
                    .rejectedValue(value)
                    .build());
          }
        }
      }
    }
  }
}
