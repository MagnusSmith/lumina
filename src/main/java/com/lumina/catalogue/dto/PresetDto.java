package com.lumina.catalogue.dto;

import com.lumina.catalogue.model.*;
import com.lumina.catalogue.model.constraint.Constraint;
import com.lumina.meter.model.Line;
import com.lumina.validation.EnumNamePattern;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.annotation.Nullable;
import org.springframework.data.annotation.Id;

import java.util.List;

public interface PresetDto {
    static Preset toModel(New aNew){
      return PresetBuilder.builder().level(aNew.level()).type(aNew.type).lines(aNew.lines()).constraints(aNew.constraints()).build();
    }

  static Preset toModel(Update update){
    return PresetBuilder.builder().level(update.level()).type(update.type).lines(update.lines()).constraints(update.constraints()).id(update.id()).build();
  }

  static Info from(Preset preset){
     return PresetDtoInfoBuilder.builder().id(preset.id()).level(preset.level()).type(preset.type()).lines(preset.lines()).constraints(preset.constraints()).build();
  }

  record New(@EnumNamePattern(regexp = "GATEWAY|DEVICE") Level level,
             @EnumNamePattern(regexp = "LORAWAN|MODBUS|SIDEWALK") MeterType type,
             List<? extends Line> lines,
             List<Constraint<? extends Line>> constraints){

  }

record Update(
      @Nullable @Id String id,
      @EnumNamePattern(regexp = "GATEWAY|DEVICE") Level level,
      @EnumNamePattern(regexp = "LORAWAN|MODBUS|SIDEWALK") MeterType type,
      List<? extends Line> lines,
      List<Constraint<? extends Line>> constraints){
  }

  @RecordBuilder
  record Info (
      String id,
      Level level,
      MeterType type,
      List<? extends Line> lines,
      List<Constraint<? extends Line>> constraints){
  }
}
