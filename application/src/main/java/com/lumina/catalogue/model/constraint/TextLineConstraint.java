package com.lumina.catalogue.model.constraint;

import static com.lumina.validation.ErrorCode.*;

import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Line;
import com.lumina.validation.ErrorBuilder;
import com.lumina.validation.Errors;
import com.lumina.validation.ValidationStageEnum;
import io.micrometer.common.util.StringUtils;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@RecordBuilder
public record TextLineConstraint(
    String name,
    String description,
    Integer minLength,
    Integer maxLength,
    boolean isRequired,
    @ValidationStageEnum ValidationStage stage)
    implements Constraint<Line.Text> {

  public void validate(Line.Text line, Errors errors, ValidationStage validationStage) {
    if (stage().shouldValidateAt(validationStage)) {
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
