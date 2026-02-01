package com.lumina.project;

import com.lumina.NotFoundException;
import com.lumina.project.model.Project;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

  /**
   * Updates a project. This method validates that the project exists before performing the update.
   *
   * @param project the project with updated data
   * @return the updated project
   * @throws NotFoundException if the project does not exist
   */
  public Project update(Project project) {
    if (!repository.existsById(project.id())) {
      throw new NotFoundException(
          "The project with id %s could not be found!".formatted(project.id()));
    }
    return repository.save(project);
  }

  public List<Project> findByClientId(String clientId) {
    return repository.findByClientId(clientId);
  }

  public Optional<Project> findById(String id) {
    return repository.findById(id);
  }

  public List<Project> findAllById(Collection<String> ids) {
    return repository.findAllById(ids);
  }

  public List<Project> findAll() {
    return repository.findAll();
  }
}
