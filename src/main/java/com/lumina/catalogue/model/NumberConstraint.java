package com.lumina.catalogue.model;


import io.soabase.recordbuilder.core.RecordBuilder;


@RecordBuilder
public record NumberConstraint(String name, String description, NumberType numberType, Double min, Double max, boolean isRequired) implements Constraint {}
