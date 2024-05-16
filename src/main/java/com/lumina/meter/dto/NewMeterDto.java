package com.lumina.meter.dto;

import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Line;
import com.lumina.meter.model.Meter;
import com.lumina.meter.model.MeterBuilder;

import java.util.ArrayList;
import java.util.List;

public record NewMeterDto (
  String locationId,
  String model
  ){
  public static Meter toModel(NewMeterDto dto){
    return MeterBuilder.builder()
        .locationId(dto.locationId)
        .model(dto.model)
        .lines(new ArrayList<>())
        .stage(ValidationStage.New)
        .build();
  }
}
