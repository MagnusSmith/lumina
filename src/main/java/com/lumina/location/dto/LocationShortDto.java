package com.lumina.location.dto;

import com.lumina.location.model.Location;
import com.lumina.meter.model.Meter;
import java.util.List;

public record LocationShortDto(String id, String projectId, String name, List<String> meterIds) {
  public static LocationShortDto from(Location location){
    return new LocationShortDto(location.id(), location.projectId(), location.name(), location.meters().stream().map(Meter::id).toList());
  }
}
