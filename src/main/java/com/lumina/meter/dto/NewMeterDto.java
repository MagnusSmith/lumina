package com.lumina.meter.dto;

import com.lumina.meter.model.Line;
import com.lumina.meter.model.Meter;
import com.lumina.meter.model.MeterBuilder;
import java.util.List;

public record NewMeterDto (
  String locationId,
  String model,
  List<Line> lines
  ){
  public static Meter toModel(NewMeterDto dto){
   return  MeterBuilder.builder().locationId(dto.locationId).model(dto.model).lines(dto.lines).build();
  }
}
