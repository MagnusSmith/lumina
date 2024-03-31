package com.lumina.catalogue.model;


import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record TextConstraint(String name, String description, Integer minLength, Integer maxLength, boolean isRequired) implements Constraint {}
