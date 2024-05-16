package com.lumina.catalogue.model;

import static com.lumina.catalogue.model.NumberType.FLOAT;
import static com.lumina.catalogue.model.NumberType.INTEGER;
import static com.lumina.validation.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lumina.meter.model.Line;
import com.lumina.validation.Errors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NumberConstraintTest {

  Errors errors;

  @BeforeEach
  void setup() {
    errors = new Errors("meter");
  }




  @Test
  @DisplayName("A number that is not an integer passes to an integer NumberConstraint will produce error")
  void valueMustBeAnIntegerNumber() {
    var n1 = new Line.Number("gtThanEqualToZero", INTEGER, 5.5d);
    var numberConstraint =
        NumberLineConstraintBuilder.builder()
            .isRequired(true)
            .description("Should be Greater or equal to 0")
            .numberType(INTEGER)
            .min(0d)
            .name("gtThanEqualToZero")
            .stage(ValidationStage.New)
            .build();
    errors.pushContext("lines[0]");
    numberConstraint.validate(n1, errors, ValidationStage.One);
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
    var n1 = new Line.Number("gtThanEqualToZero", INTEGER, 0d);
    var numberConstraint =
        NumberLineConstraintBuilder.builder()
            .isRequired(true)
            .description("Should be Greater or equal to 0")
            .numberType(INTEGER)
            .min(0d)
            .name("gtThanEqualToZero")
            .stage(ValidationStage.New)
            .build();
    errors.pushContext("lines[0]");
    numberConstraint.validate(n1, errors, ValidationStage.New);
    errors.popContext();
    assertThat(errors.getErrorCount()).isZero();
  }

  @Test
  @DisplayName("Integer value less than min should produce error")
  void intLessThanEqualToMinShouldError() {
    var n1 = new Line.Number("lessMin5", INTEGER, 4d);
    var numberConstraint =
        NumberLineConstraintBuilder.builder()
            .isRequired(true)
            .description("Should be greater than or equal to 5")
            .numberType(INTEGER)
            .min(5d)
            .name("lessMinFive")
            .stage(ValidationStage.One)
            .build();
    errors.pushContext("lines[0]");
    numberConstraint.validate(n1, errors, ValidationStage.One);
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
    var n1 = new Line.Number("lessMin5", FLOAT, 4d);
    var numberConstraint =
        NumberLineConstraintBuilder.builder()
            .isRequired(true)
            .description("Should be greater than or equal to 5")
            .numberType(FLOAT)
            .min(5d)
            .name("lessMinFive")
            .stage(ValidationStage.One)
            .build();
    errors.pushContext("lines[0]");
    numberConstraint.validate(n1, errors, ValidationStage.One);
    assertThat(errors.getErrorCount()).isOne();
    assertThat(errors.hasFieldError("lessMinFive")).isTrue();
    var err = errors.fieldError("lessMinFive");
    assertThat(err.rejectedValue()).isNotNull();
    assertThat(err.rejectedValue()).isInstanceOf(Double.class).isEqualTo(4.0);
    assertThat(err.errorCode()).isEqualTo(LESS_THAN);
    assertThat(err.errorCodeArgs()).contains(4.0, 5.0);
    assertThat(err.fieldContext()).isEqualTo("meter.lines[0]");
  }


  @Test
  @DisplayName("Double value less than min should not produce an error before validation stage")
  void doubleLessThanEqualToMinShouldNotFailBeforeValidationStage() {
    var n1 = new Line.Number("lessMin5", FLOAT, 4d);
    var numberConstraint =
        NumberLineConstraintBuilder.builder()
            .isRequired(true)
            .description("Should be greater than or equal to 5")
            .numberType(FLOAT)
            .min(5d)
            .name("lessMinFive")
            .stage(ValidationStage.One)
            .build();
    errors.pushContext("lines[0]");
    numberConstraint.validate(n1, errors,  ValidationStage.One);
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
