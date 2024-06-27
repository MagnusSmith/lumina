package com.lumina.location.dto;

import com.lumina.location.model.Location;
import com.lumina.meter.model.Meter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record LocationShortDto(String id, String projectId, String name, List<String> meterIds) {
    public static LocationShortDto from(Location location) {
        return new LocationShortDto(
                location.id(),
                location.projectId(),
                location.name(),
                Optional.ofNullable(location.meters())
                        .map(mets -> mets.stream().map(Meter::id).toList())
                        .orElseGet(ArrayList::new));
    }
}
