package com.lumina.catalogue.model;

import static com.lumina.validation.ErrorCode.*;

import com.lumina.meter.model.Line;
import com.lumina.meter.model.NumberLine;
import com.lumina.validation.ErrorBuilder;
import com.lumina.validation.Errors;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Objects;

@RecordBuilder
public record NumberLineConstraint(
    String name,
    String description,
    NumberType numberType,
    Double min,
    Double max,
    boolean isRequired)
    implements Constraint<Line> {

  public void validate(Line line, Errors errors) {
    if (line instanceof NumberLine) {
      var value = line.value();
      if (value instanceof Double valD) {
        if (numberType == NumberType.INTEGER) {
          if ((valD == Math.floor(valD)) && !Double.isInfinite(valD)) {
            // integer type
            int valInt = valD.intValue();
            if (Objects.nonNull(min) && min.intValue() > valInt) {
              errors.add(
                  ErrorBuilder.builder()
                      .field(name)
                      .errorCode(LESS_THAN)
                      .errorCodeArgs(new Object[] {valInt, min.intValue()})
                      .rejectedValue(valInt)
                      .build());
            } else if (Objects.nonNull(max) && max.intValue() < valInt) {
              errors.add(
                  ErrorBuilder.builder()
                      .field(name)
                      .errorCode(GREATER_THAN)
                      .errorCodeArgs(new Object[] {valInt, max.intValue()})
                      .rejectedValue(valInt)
                      .build());
            }
          } else {
            errors.add(
                ErrorBuilder.builder()
                    .field(name)
                    .errorCode(NOT_INTEGER)
                    .errorCodeArgs(new Object[] {valD})
                    .rejectedValue(valD)
                    .build());
            }
        }
        if (numberType == NumberType.FLOAT) {
          if (Objects.nonNull(min) && min > valD) {
            errors.add(
                ErrorBuilder.builder()
                    .field(name)
                    .errorCode(LESS_THAN)
                    .errorCodeArgs(new Object[] {valD, min})
                    .rejectedValue(valD)
                    .build());

          } else if (Objects.nonNull(max) && max < valD) {
            errors.add(
                ErrorBuilder.builder()
                    .field(name)
                    .errorCode(GREATER_THAN)
                    .errorCodeArgs(new Object[] {valD, max})
                    .rejectedValue(valD)
                    .build());
          }
        }
      }
    } else {
      errors.add(
          ErrorBuilder.builder()
              .field(name)
              .errorCode(WRONG_TYPE)
              .errorCodeArgs(new Object[] {numberType})
              .build());
    }
  }
}
