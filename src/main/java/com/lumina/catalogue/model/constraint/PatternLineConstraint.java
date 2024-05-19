package com.lumina.catalogue.model.constraint;

import static com.lumina.validation.ErrorCode.*;

import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Line;
import com.lumina.validation.ErrorBuilder;
import com.lumina.validation.Errors;
import com.lumina.validation.ValidationStageEnum;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@RecordBuilder
@Document
public record PatternLineConstraint(
    String name,
    String description,
    String pattern,
    boolean isRequired,
    @ValidationStageEnum ValidationStage stage)
    implements Constraint<Line.Pattern> {
  @Override
  public void validate(Line.Pattern line, Errors errors, ValidationStage stage) {
    if (stage().shouldValidateAt(stage)) {
      var value = line.value();

      Pattern regExPattern;
      try {

        regExPattern = Pattern.compile(pattern);

        if (!regExPattern.matcher(value).matches()) {
          errors.add(
              ErrorBuilder.builder()
                  .field(name)
                  .errorCode(INVALID_PATTERN)
                  .errorCodeArgs(new Object[] {value, pattern})
                  .rejectedValue(value)
                  .build());
        }
      } catch (PatternSyntaxException e) {
        throw new IllegalArgumentException("Given regex is invalid", e);
      }
    }
  }


}
