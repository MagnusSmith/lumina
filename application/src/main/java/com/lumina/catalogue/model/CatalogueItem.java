package com.lumina.catalogue.model;


import com.lumina.catalogue.model.constraint.Constraint;
import com.lumina.meter.model.Line;
import com.lumina.validation.EnumNamePattern;
import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "catalogue")
@TypeAlias("CatalogueItem")
@RecordBuilder
public record CatalogueItem (

    // @NotBlank(message = "Id is mandatory")
    @Nullable String id,
    //@UniqueModel
    @NotBlank(message = "Model is mandatory") String model,
    @EnumNamePattern(regexp = "GATEWAY|DEVICE") Level level,
    @EnumNamePattern(regexp = "LORAWAN|MODBUS|SIDEWALK") MeterType type,
    @NotBlank(message = "Description is mandatory") String description,
    @NotBlank(message = "Manufacturer is mandatory") String manufacturer,
    List<? extends Line> lines,
    List<Constraint<? extends Line>> constraints) implements Item {

  public CatalogueItem(String model, Level level, MeterType type, String description,
                       String manufacturer, List<? extends Line> lines, List<Constraint<? extends Line>> constraints){
    this(null,  model, level, type, description, manufacturer, lines, constraints);
  }

}
