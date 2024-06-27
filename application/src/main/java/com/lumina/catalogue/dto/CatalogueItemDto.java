package com.lumina.catalogue.dto;

import com.lumina.catalogue.model.CatalogueItem;
import com.lumina.catalogue.model.Level;
import com.lumina.catalogue.model.MeterType;
import com.lumina.catalogue.model.constraint.Constraint;
import com.lumina.meter.model.Line;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.List;

@RecordBuilder
public record CatalogueItemDto(
    String id,
    String model,
    Level level,
    MeterType type,
    String description,
    String manufacturer,
    List<? extends Constraint<? extends Line>> constraints) {
  public static CatalogueItemDto from(CatalogueItem item) {
    return CatalogueItemDtoBuilder.builder()
        .id(item.id())
        .model(item.model())
        .level(item.level())
        .type(item.type())
        .description(item.description())
        .manufacturer(item.manufacturer())
        .constraints(item.constraints())
        .build();
  }
}
