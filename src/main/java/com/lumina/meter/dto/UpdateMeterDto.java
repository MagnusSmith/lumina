package com.lumina.meter.dto;

import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Line;
import com.lumina.meter.model.Meter;
import com.lumina.meter.model.MeterBuilder;
import com.lumina.validation.ValidationStageEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateMeterDto(@NotBlank(message = "The Meter id is required") String id,
                             @NotBlank(message = "The LocationId is required") String locationId,
                             @NotBlank(message = "The model field is required")String model,
                             List<Line> lines,
                             @ValidationStageEnum ValidationStage stage) {
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