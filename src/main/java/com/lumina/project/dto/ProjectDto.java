package com.lumina.project.dto;

import com.lumina.location.dto.LocationShortDto;
import com.lumina.project.model.Project;
import java.util.List;

public record ProjectDto(String id, String clientId, String name, List<LocationShortDto> locations) {

  public static ProjectDto from(Project project){
    return new ProjectDto(project.id(), project.clientId(),  project.name(), project.locations().stream().map(LocationShortDto::from).toList());
  }
}
