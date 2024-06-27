package com.lumina.meter.model;

import com.lumina.catalogue.model.ValidationStage;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "meter")
@TypeAlias("Meter")
@RecordBuilder
public record Meter(
        @Id String id, String locationId, String model, List<Line> lines, ValidationStage stage) {}
