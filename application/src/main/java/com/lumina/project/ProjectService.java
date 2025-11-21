package com.lumina.project;

import com.lumina.NotFoundException;
import com.lumina.project.model.Project;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

  private final ProjectRepository repository;

  public ProjectService(ProjectRepository repository) {
    this.repository = repository;
  }

  public Project create(Project project) {
    return repository.save(project);
  }

  public Project update(Project project) {
    repository
        .findById(project.id())
        .orElseThrow(
            () ->
                new NotFoundException(
                    "The project with id %s could not be found!".formatted(project.id())));
    return repository.save(project);
  }
}
