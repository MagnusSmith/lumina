package com.lumina.catalogue.model;

import com.lumina.meter.model.NumberLine;
import com.lumina.meter.model.TextLine;
import com.lumina.validation.Errors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.lumina.catalogue.model.NumberType.FLOAT;
import static com.lumina.catalogue.model.NumberType.INTEGER;
import static org.assertj.core.api.Assertions.assertThat;

import static com.lumina.validation.ErrorCode.*;

public class NumberLineConstraintTest {

  Errors errors;

  @BeforeEach
  void setup() {
    errors = new Errors("meter");
  }


  @Test
  @DisplayName("A line supplied must be a NumberLine")
  void valueMustBeANumber() {
    var t1 = new TextLine("mustBeNumLine", "5");
    var numberConstraint =
        NumberLineConstraintBuilder.builder()
            .isRequired(true)
            .description("Should be Greater or equal to 0")
            .numberType(INTEGER)
            .min(0d)
            .name("mustBeNumLine")
            .build();
    errors.pushContext("lines[0]");
    numberConstraint.validate(t1, errors);
    assertThat(errors.getErrorCount()).isOne();
    assertThat(errors.hasFieldError("mustBeNumLine")).isTrue();
    var err = errors.fieldError("mustBeNumLine");
    assertThat(err.errorCode()).isEqualTo(WRONG_TYPE);
    assertThat(err.errorCodeArgs()).contains(numberConstraint.numberType());

  }

  @Test
  @DisplayName("A number that is not an integer passes to an integer NumberConstraint will produce error")
  void valueMustBeANIntegerNumber() {
    var n1 = new NumberLine("gtThanEqualToZero", INTEGER, 5.5d);
    var numberConstraint =
        NumberLineConstraintBuilder.builder()
            .isRequired(true)
            .description("Should be Greater or equal to 0")
            .numberType(INTEGER)
            .min(0d)
            .name("gtThanEqualToZero")
            .build();
    errors.pushContext("lines[0]");
    numberConstraint.validate(n1, errors);
    errors.popContext();
    assertThat(errors.getErrorCount()).isOne();
    assertThat(errors.hasFieldError("gtThanEqualToZero")).isTrue();
    var err = errors.fieldError("gtThanEqualToZero");
    assertThat(err.rejectedValue()).isInstanceOf(Double.class).isEqualTo(5.5);
    assertThat(err.errorCode()).isEqualTo( NOT_INTEGER);
    assertThat(err.errorCodeArgs()).contains(5.5);

  }

  @Test
  @DisplayName("An integer value greater or equal than min should produce no errors")
  void greaterThanEqualToMinShouldPass() {
    var n1 = new NumberLine("gtThanEqualToZero", INTEGER, 0d);
    var numberConstraint =
        NumberLineConstraintBuilder.builder()
            .isRequired(true)
            .description("Should be Greater or equal to 0")
            .numberType(INTEGER)
            .min(0d)
            .name("gtThanEqualToZero")
            .build();
    errors.pushContext("lines[0]");
    numberConstraint.validate(n1, errors);
    errors.popContext();
    assertThat(errors.getErrorCount()).isZero();
  }

  @Test
  @DisplayName("Integer value less than min should produce error")
  void intLessThanEqualToMinShouldFError() {
    var n1 = new NumberLine("lessMin5", INTEGER, 4d);
    var numberConstraint =
        NumberLineConstraintBuilder.builder()
            .isRequired(true)
            .description("Should be greater than or equal to 5")
            .numberType(INTEGER)
            .min(5d)
            .name("lessMinFive")
            .build();
    errors.pushContext("lines[0]");
    numberConstraint.validate(n1, errors);
    assertThat(errors.getErrorCount()).isOne();
    assertThat(errors.hasFieldError("lessMinFive")).isTrue();
    var err = errors.fieldError("lessMinFive");
    assertThat(err.rejectedValue()).isInstanceOf(Integer.class).isEqualTo(4);
    assertThat(err.errorCode()).isEqualTo(LESS_THAN);
    assertThat(err.errorCodeArgs()).contains(4, 5);

    assertThat(err.fieldContext()).isEqualTo("meter.lines[0]");
  }

  @Test
  @DisplayName("Double value less than min should produce error")
  void doubleLessThanEqualToMinShouldFail() {
    var n1 = new NumberLine("lessMin5", FLOAT, 4d);
    var numberConstraint =
        NumberLineConstraintBuilder.builder()
            .isRequired(true)
            .description("Should be greater than or equal to 5")
            .numberType(FLOAT)
            .min(5d)
            .name("lessMinFive")
            .build();
    errors.pushContext("lines[0]");
    numberConstraint.validate(n1, errors);
    assertThat(errors.getErrorCount()).isOne();
    assertThat(errors.hasFieldError("lessMinFive")).isTrue();
    var err = errors.fieldError("lessMinFive");
    assertThat(err.rejectedValue()).isNotNull();
    assertThat(err.rejectedValue()).isInstanceOf(Double.class).isEqualTo(4.0);
    assertThat(err.errorCode()).isEqualTo(LESS_THAN);
    assertThat(err.errorCodeArgs()).contains(4.0, 5.0);
    assertThat(err.fieldContext()).isEqualTo("meter.lines[0]");
  }
}
