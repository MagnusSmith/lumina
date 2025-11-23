package com.lumina.meter.dto;

import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Line;
import com.lumina.meter.model.Meter;
import com.lumina.meter.model.MeterBuilder;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public record NewMeterDto(
    @NotBlank(message = "The LocationId is required") String locationId,
    @NotBlank(message = "The model field is required") String model,
    List<Line> lines) {
  public static Meter toModel(NewMeterDto dto) {
    return MeterBuilder.builder()
        .locationId(dto.locationId)
        .model(dto.model)
        .lines(dto.lines != null ? dto.lines : new ArrayList<>())
        .stage(ValidationStage.Intake)
        .build();
  }
}
