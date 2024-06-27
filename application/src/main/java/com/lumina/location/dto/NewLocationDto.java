package com.lumina.location.dto;

import com.lumina.location.model.Location;
import com.lumina.location.model.LocationBuilder;

public record NewLocationDto(String projectId, String name) {
    public static Location toModel(NewLocationDto dto) {
        return LocationBuilder.builder().projectId(dto.projectId()).name(dto.name()).build();
    }
}
