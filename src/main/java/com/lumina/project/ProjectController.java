package com.lumina.project;


import com.lumina.client.Client;
import com.lumina.client.ClientService;
import com.lumina.client.dto.NewClient;
import com.lumina.client.dto.UpdateClient;
import com.lumina.project.dto.NewProject;
import com.lumina.project.dto.UpdateProject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public Project create(@RequestBody NewProject newProject) {
    return projectService.create(newProject.toProject());
  }

  @PutMapping("project")
  public Project update(@RequestBody UpdateProject updateProject) {
    return projectService.update(updateProject.toProject());
  }

  //  @GetMapping("project/{id}")
  //  public ResponseEntity<Client> getById(@PathVariable String id){
  //    return projectService
  //        .findById(id)
  //        .map(ResponseEntity::ok)
  //        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  //  }
}
