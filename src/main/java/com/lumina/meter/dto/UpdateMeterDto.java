package com.lumina.meter.dto;

import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Line;
import com.lumina.meter.model.Meter;
import com.lumina.meter.model.MeterBuilder;
import java.util.List;

public record UpdateMeterDto(String id, String locationId, String model, List<Line> lines, ValidationStage stage) {
  public static Meter toModel(UpdateMeterDto dto) {
    return MeterBuilder.builder()
        .id(dto.id())
        .locationId(dto.locationId)
        .model(dto.model)
        .lines(dto.lines)
        .stage(dto.stage)
        .build();
  }
}