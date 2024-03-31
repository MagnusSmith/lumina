package com.lumina.project.dto;


import com.lumina.project.Project;
import com.lumina.project.ProjectBuilder;


public record NewProject(
    String clientId,
    String name
)  {
  public Project toProject() {
    return ProjectBuilder.builder().name(name).clientId(clientId).build();
  }
}
