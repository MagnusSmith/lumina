package com.lumina.catalogue.model;

import com.lumina.catalogue.model.constraint.Constraint;
import com.lumina.meter.model.Line;
import com.lumina.validation.EnumNamePattern;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.annotation.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document(collection = "catalogue")
@TypeAlias("Preset")
@RecordBuilder
public record Preset(
    @Nullable  @Id String id,
    @EnumNamePattern(regexp = "GATEWAY|DEVICE") Level level,
    @EnumNamePattern(regexp = "LORAWAN|MODBUS|SIDEWALK") MeterType type,
    List<? extends Line> lines,
    List<Constraint<? extends Line>> constraints)  implements Item {

}
