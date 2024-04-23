package com.lumina.project;

import com.lumina.project.model.Project;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProjectController {
  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @PostMapping("project")
  @ResponseStatus(HttpStatus.CREATED)
  public Project create(@RequestBody Project newProject) {
    return projectService.create(newProject);
  }

  @PutMapping("project")
  public Project update(@RequestBody Project updateProject) {
    return projectService.update(updateProject);
  }
}
