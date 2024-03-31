package com.lumina.meter.model.info;

import com.lumina.catalogue.model.Level;
import com.lumina.catalogue.model.MeterType;

import io.soabase.recordbuilder.core.RecordBuilder;
import org.springframework.data.annotation.Id;

import java.util.List;
@RecordBuilder
public record MeterInfo(
    @Id String id,
    String locationId,
    String model,
    Level level,
    MeterType type,
    String name,
    String description,
    String manufacturer,
    List<LineInfo> lines)  implements MeterInfoBuilder.With {




}
