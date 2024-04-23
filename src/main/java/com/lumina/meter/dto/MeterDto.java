package com.lumina.meter.dto;

import com.lumina.catalogue.model.CatalogueItem;
import com.lumina.catalogue.model.Level;
import com.lumina.catalogue.model.MeterType;
import com.lumina.meter.model.Meter;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.List;
import org.springframework.data.annotation.Id;

@RecordBuilder
public record MeterDto(
    @Id String id,
    String locationId,
    String model,
    Level level,
    MeterType type,
    String name,
    String description,
    String manufacturer,
    List<LineDto> lines)  implements MeterDtoBuilder.With {


 public static MeterDto from(CatalogueItem item, Meter meter) {
    List<LineDto> infoLines =
        item.constraints().stream()
            .map(
                c ->
                    meter.lines().stream()
                        .filter(l -> l.name().equalsIgnoreCase(c.name()))
                        .findAny()
                        .map(sl -> new LineDto(sl, c))
                        .orElseGet(() -> new LineDto(null, c)))
            .toList();

   return   MeterDtoBuilder.builder()
            .id(meter.id())
            .locationId(meter.locationId())
            .model(meter.model())
            .name(item.name())
            .description(item.description())
            .level(item.level())
            .type(item.type())
            .manufacturer(item.manufacturer())
            .lines(infoLines)
            .build();


  }

}
