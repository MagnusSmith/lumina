package com.lumina.catalogue.dto;

import com.lumina.catalogue.model.*;
import com.lumina.catalogue.validation.UniqueModel;
import com.lumina.meter.model.Line;
import com.lumina.validation.EnumNamePattern;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record NewCatalogueItemDto(
    @UniqueModel
    @NotBlank(message = "Model is mandatory") String model,
    @EnumNamePattern(regexp = "GATEWAY|DEVICE") Level level,
    @EnumNamePattern(regexp = "LORAWAN|MODBUS|SIDEWALK") MeterType type,
    @NotBlank(message = "Name is mandatory") String name,
    @NotBlank(message = "Description is mandatory") String description,
    @NotBlank(message = "Manufacturer is mandatory") String manufacturer,

    List<Constraint<Line>> constraints
) {

  public static CatalogueItem toModel(NewCatalogueItemDto dto){
    return CatalogueItemBuilder.builder().model(dto.model()).level(dto.level()).type(dto.type()).name(dto.model()).description(dto.description()).manufacturer(dto.manufacturer()).build();
  }
}
