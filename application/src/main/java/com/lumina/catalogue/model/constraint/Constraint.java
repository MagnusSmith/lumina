package com.lumina.catalogue.model.constraint;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Line;
import com.lumina.validation.Errors;
import java.util.function.Predicate;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @Type(value = NumberLineConstraint.class, name = "NUMERIC"),
  @Type(value = TextLineConstraint.class, name = "TEXT"),
  @Type(value = PatternLineConstraint.class, name = "PATTERN")
})
public sealed interface Constraint<T extends Line>
    permits TextLineConstraint, NumberLineConstraint, PatternLineConstraint {
  String name();

  void validate(T value, Errors errors, ValidationStage stage);

  boolean isRequired();

  ValidationStage stage();

  default Predicate<? super Line> constrains() {
    return c -> name().equals(c.name());
  }
}
