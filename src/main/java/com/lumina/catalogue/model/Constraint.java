package com.lumina.catalogue.model;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "constraintType")
@JsonSubTypes({
    @Type(value = NumberConstraint.class, name = "NUMERIC"),
    @Type(value = TextConstraint.class, name = "TEXT")
})
public interface Constraint {
  String name();
}
