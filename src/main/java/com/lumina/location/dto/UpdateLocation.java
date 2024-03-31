package com.lumina.location.dto;

import com.lumina.location.Location;
import com.lumina.location.LocationBuilder;
import com.lumina.project.Project;
import com.lumina.project.ProjectBuilder;

public record UpdateLocation(
    String id,
    String projectId,
    String name
) {
  public Location toLocation() {
    return  LocationBuilder.builder().id(id).name(name).projectId(projectId).build();
  }
}
