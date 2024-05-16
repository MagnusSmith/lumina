package com.lumina.meter.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lumina.catalogue.model.NumberType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @Type(value = Line.Number.class, name = "NUMERIC"),
    @Type(value = Line.Text.class, name = "TEXT"),
    @Type(value = Line.ReadOnly.class, name = "READ_ONLY")
})
public sealed interface Line permits Line.Number, Line.Text, Line.ReadOnly {
  String name();
  Object value();

  record Number(String name, NumberType numberType, Double value) implements Line {}

  record Text(String name, String value) implements Line {}

  record ReadOnly(String name, String value) implements Line {}
}
