package com.lumina.catalogue.model;

import static com.lumina.catalogue.model.NumberType.INTEGER;
import static com.lumina.validation.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lumina.meter.model.NumberLine;
import com.lumina.meter.model.TextLine;
import com.lumina.validation.Errors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TextLineConstraintTest {
  Errors errors;

  @BeforeEach
  void setup() {
    errors = new Errors("meter");
  }

  @Test
  @DisplayName("A line supplied must be a TextLine")
  void valueMustBeTextOrError() {
    var n1 = new NumberLine("notTooShort", INTEGER, 0d);
    var textConstraint =
        TextLineConstraintBuilder.builder()
            .name("notTooShort")
            .description("A String with a minimum length")
            .minLength(6)
            .isRequired(true)
            .build();

    errors.pushContext("lines[0]");
    textConstraint.validate(n1, errors);
    assertThat(errors.getErrorCount()).isOne();
    assertThat(errors.hasFieldError("notTooShort")).isTrue();
    var err = errors.fieldError("notTooShort");
    assertThat(err.errorCode()).isEqualTo(WRONG_TYPE);
    assertThat(err.fieldContext()).isEqualTo("meter.lines[0]");
  }


  @Test
  @DisplayName("A value less than minimum length will produce a field error")
  void valueMustSatisfyMinimumLengthOrError() {
    var n1 = new TextLine("notTooShort", "Hello");
    var textConstraint =
        TextLineConstraintBuilder.builder()
            .name("notTooShort")
            .description("A String with a minimum length")
            .minLength(6)
            .isRequired(true)
            .build();

    errors.pushContext("lines[0]");
    textConstraint.validate(n1, errors);
    assertThat(errors.getErrorCount()).isOne();
    assertThat(errors.hasFieldError("notTooShort")).isTrue();
    var err = errors.fieldError("notTooShort");
    assertThat(err.errorCode()).isEqualTo(MIN_LENGTH);
    assertThat(err.errorCodeArgs()).contains("Hello", 6);

    assertThat(err.fieldContext()).isEqualTo("meter.lines[0]");
  }
}
