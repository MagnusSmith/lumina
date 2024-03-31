package com.lumina.location.dto;


import com.lumina.location.Location;
import com.lumina.location.LocationBuilder;
import com.lumina.project.Project;
import com.lumina.project.ProjectBuilder;

public record NewLocation(
    String projectId,
    String name
)  {
  public Location toLocation() {
    return  LocationBuilder.builder().name(name).projectId(projectId).build();
  }
}
