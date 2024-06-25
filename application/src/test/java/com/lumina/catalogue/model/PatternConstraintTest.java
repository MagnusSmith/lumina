package com.lumina.catalogue.model;

import com.lumina.catalogue.model.constraint.PatternLineConstraintBuilder;
import com.lumina.meter.model.Line;
import com.lumina.validation.Errors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.lumina.validation.ErrorCode.INVALID_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;

public class PatternConstraintTest {
  Errors errors;

  @BeforeEach
  void setup() {
    errors = new Errors("meter");
  }


  @Test
  @DisplayName("A value should match pattern")
  void valueShouldMatchPattern() {
    var n1 = new Line.Pattern("pl1", "1234-1234-1234-4321");
    var patternConstraint =
        PatternLineConstraintBuilder.builder()
            .name("pl1")
            .description("A special id split into 4 digit words separated by a '-'")
            .pattern("\\d{4}-\\d{4}-\\d{4}-\\d{4}")
            .isRequired(true)
            .stage(ValidationStage.Connection)
            .build();

    errors.pushContext("lines[0]");
    patternConstraint.validate(n1, errors, ValidationStage.Connection);
    assertThat(errors.getErrorCount()).isZero();

  }



  @Test
  @DisplayName("A value should match pattern or will produce a field error")
  void valueShouldMatchPatternOrError() {
    var n1 = new Line.Pattern("pl1", "123-1234-1234-4321");
    var patternConstraint =
        PatternLineConstraintBuilder.builder()
            .name("pl1")
            .description("A special id split into 4 digit words separated by a '-'")
            .pattern("\\d{4}-\\d{4}-\\d{4}-\\d{4}")
            .isRequired(true)
            .stage(ValidationStage.Connection)
            .build();

    errors.pushContext("lines[0]");
    patternConstraint.validate(n1, errors, ValidationStage.Connection);
    assertThat(errors.getErrorCount()).isOne();
    assertThat(errors.hasFieldError("pl1")).isTrue();
    var err = errors.fieldError("pl1");
    assertThat(err.errorCode()).isEqualTo(INVALID_PATTERN);
    assertThat(err.errorCodeArgs()).contains("123-1234-1234-4321", "\\d{4}-\\d{4}-\\d{4}-\\d{4}");

    assertThat(err.fieldContext()).isEqualTo("meter.lines[0]");
  }




}
