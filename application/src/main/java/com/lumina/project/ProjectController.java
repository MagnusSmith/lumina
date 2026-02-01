package com.lumina.project;

import com.lumina.project.dto.NewProjectDto;
import com.lumina.project.dto.ProjectDto;
import com.lumina.project.dto.UpdateProjectDto;
import com.lumina.project.model.Project;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
@Tag(name = "Project", description = "Project management APIs")
public class ProjectController {
  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
  }

  @PostMapping("project")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new project")
  @ApiResponse(responseCode = "201", description = "Project created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input")
  public Project create(@RequestBody @Valid NewProjectDto newProjectDto) {
    return projectService.create(NewProjectDto.toModel(newProjectDto));
  }

  @PutMapping("project")
  @Operation(summary = "Update an existing project")
  @ApiResponse(responseCode = "200", description = "Project updated successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input")
  @ApiResponse(responseCode = "404", description = "Project not found")
  public Project update(@RequestBody @Valid UpdateProjectDto updateProjectDto) {
    return projectService.update(UpdateProjectDto.toModel(updateProjectDto));
  }

  @GetMapping("project/client/{clientId}")
  @Operation(summary = "Get all projects for a client")
  @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
  public List<ProjectDto> getByClientId(
      @Parameter(description = "Client ID") @PathVariable String clientId) {
    return projectService.findByClientId(clientId).stream().map(ProjectDto::from).toList();
  }
}
