package com.lumina.meter.dto;

import com.lumina.catalogue.model.*;
import com.lumina.catalogue.model.constraint.Constraint;
import com.lumina.meter.model.Line;
import com.lumina.meter.model.Meter;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;

@RecordBuilder
public record MeterDto(
        @Id String id,
        String locationId,
        String model,
        Level level,
        MeterType type,
        String description,
        String manufacturer,
        List<Line> lines,
        List<Constraint<? extends Line>> constraints,
        ValidationStage stage)
        implements MeterDtoBuilder.With {

    public static MeterDto from(CatalogueItem item, Meter meter, boolean withConstraints) {

        return MeterDtoBuilder.builder()
                .id(meter.id())
                .locationId(meter.locationId())
                .model(meter.model())
                .description(item.description())
                .level(item.level())
                .type(item.type())
                .manufacturer(item.manufacturer())
                .lines(meter.lines())
                .stage(meter.stage())
                .constraints(withConstraints ? item.constraints() : new ArrayList<>())
                .build();
    }
}
