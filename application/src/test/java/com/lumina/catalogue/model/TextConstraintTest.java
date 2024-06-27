package com.lumina.catalogue.model;

import static com.lumina.validation.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lumina.catalogue.model.constraint.TextLineConstraintBuilder;
import com.lumina.meter.model.Line;
import com.lumina.validation.Errors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TextConstraintTest {
  Errors errors;

  @BeforeEach
  void setup() {
    errors = new Errors("meter");
  }

  @Test
  @DisplayName("A value less than minimum length should produce a field error")
  void valueShouldSatisfyMinimumLengthOrError() {
    var n1 = new Line.Text("notTooShort", "Hello");
    var textConstraint =
        TextLineConstraintBuilder.builder()
            .name("notTooShort")
            .description("A String with a minimum length")
            .minLength(6)
            .isRequired(true)
            .stage(ValidationStage.Intake)
            .build();

    errors.pushContext("lines[0]");
    textConstraint.validate(n1, errors, ValidationStage.Connection);
    assertThat(errors.getErrorCount()).isOne();
    assertThat(errors.hasFieldError("notTooShort")).isTrue();
    var err = errors.fieldError("notTooShort");
    assertThat(err.errorCode()).isEqualTo(MIN_LENGTH);
    assertThat(err.errorCodeArgs()).contains("Hello", 6);

    assertThat(err.fieldContext()).isEqualTo("meter.lines[0]");
  }
}
