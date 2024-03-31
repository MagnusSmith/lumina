package com.lumina.project.dto;

import com.lumina.project.Project;
import com.lumina.project.ProjectBuilder;

public record UpdateProject(
    String id,
    String clientId,
    String name
) {
  public Project toProject() {
    return ProjectBuilder.builder().id(id).name(name).clientId(clientId).build();
  }
}
