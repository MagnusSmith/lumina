package com.lumina.catalogue.model;

import com.lumina.meter.model.Line;
import com.lumina.meter.model.TextLine;
import com.lumina.validation.ErrorBuilder;
import com.lumina.validation.Errors;
import io.micrometer.common.util.StringUtils;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Objects;

import static com.lumina.validation.ErrorCode.*;

@RecordBuilder
public record TextLineConstraint(
    String name, String description, Integer minLength, Integer maxLength, boolean isRequired)
    implements Constraint<Line> {

  public void validate(Line line, Errors errors) {
    if (line instanceof TextLine) {
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
    } else {
      errors.add(
          ErrorBuilder.builder()
              .field(name)
              .errorCode(WRONG_TYPE)
              .errorCodeArgs(new Object[] {})
              .build());
    }
  }
}
