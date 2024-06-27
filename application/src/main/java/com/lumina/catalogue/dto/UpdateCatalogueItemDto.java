package com.lumina.catalogue.dto;

import com.lumina.catalogue.model.*;
import com.lumina.catalogue.model.constraint.Constraint;
import com.lumina.meter.model.Line;
import com.lumina.validation.EnumNamePattern;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UpdateCatalogueItemDto(
    @NotBlank(message = "id is mandatory") String id,
    @NotBlank(message = "Model is mandatory") String model,
    @EnumNamePattern(regexp = "GATEWAY|DEVICE") Level level,
    @EnumNamePattern(regexp = "LORAWAN|MODBUS|SIDEWALK") MeterType type,
    @NotBlank(message = "Description is mandatory") String description,
    @NotBlank(message = "Manufacturer is mandatory") String manufacturer,
    List<Constraint<Line>> constraints) {
  public static CatalogueItem toModel(UpdateCatalogueItemDto dto) {
    return CatalogueItemBuilder.builder()
        .id(dto.id())
        .model(dto.model())
        .level(dto.level())
        .type(dto.type())
        .description(dto.description())
        .manufacturer(dto.manufacturer())
        .build();
  }
}
