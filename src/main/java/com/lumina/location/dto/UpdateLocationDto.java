package com.lumina.location.dto;

import com.lumina.location.model.Location;
import com.lumina.location.model.LocationBuilder;

public record UpdateLocationDto(String id, String projectId, String name) {
  public static Location toModel(UpdateLocationDto dto){
    return LocationBuilder.builder().id(dto.id()).projectId(dto.projectId()).name(dto.name()).build();
  }
}
