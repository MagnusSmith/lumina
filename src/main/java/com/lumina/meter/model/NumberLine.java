package com.lumina.meter.model;

import com.lumina.catalogue.model.NumberType;

public record NumberLine(String name, NumberType numberType, Double value) implements Line {}
