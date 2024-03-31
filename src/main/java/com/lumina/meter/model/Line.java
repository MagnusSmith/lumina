package com.lumina.meter.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "constraintType")
@JsonSubTypes({
    @Type(value = NumberLine.class, name = "NUMERIC"),
    @Type(value = TextLine.class, name = "TEXT")
})
public interface Line {
  String name();
}
