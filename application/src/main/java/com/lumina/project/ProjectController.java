package com.lumina.project;

import com.lumina.project.dto.NewProjectDto;
import com.lumina.project.dto.UpdateProjectDto;
import com.lumina.project.model.Project;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class ProjectController {
  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @PostMapping("project")
  @ResponseStatus(HttpStatus.CREATED)
  public Project create(@RequestBody @Valid NewProjectDto newProjectDto) {
    return projectService.create(NewProjectDto.toModel(newProjectDto));
  }

  @PutMapping("project")
  public Project update(@RequestBody @Valid UpdateProjectDto updateProjectDto) {
    return projectService.update(UpdateProjectDto.toModel(updateProjectDto));
  }
}
