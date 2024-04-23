package com.lumina.meter.dto;

import com.lumina.catalogue.model.Constraint;
import com.lumina.meter.model.Line;

public record LineDto(Line line, Constraint constraint) {}
