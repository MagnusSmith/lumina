package com.lumina.meter.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @Type(value = NumberLine.class, name = "NUMERIC"),
    @Type(value = TextLine.class, name = "TEXT")
})
public sealed interface Line permits NumberLine, TextLine{
  String name();
  Object value();
}
