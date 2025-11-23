package com.lumina.meter.model;

import io.soabase.recordbuilder.core.RecordBuilder;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "meterStatus")
@TypeAlias("Status")
@RecordBuilder
public record Status(
    @Id String id, @Indexed String meterId, @Indexed Instant timestamp, Map<String, Object> data) {}
