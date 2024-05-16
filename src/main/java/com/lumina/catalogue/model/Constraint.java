package com.lumina.catalogue.model;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.lumina.validation.Errors;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @Type(value = NumberLineConstraint.class, name = "NUMERIC"),
    @Type(value = TextLineConstraint.class, name = "TEXT")
})
public sealed interface Constraint<Line> permits TextLineConstraint, NumberLineConstraint {
  String name();
  void validate(Line value, Errors errors, ValidationStage stage);
  boolean isRequired();
  ValidationStage stage();
}
