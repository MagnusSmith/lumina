package com.lumina.project;

import com.lumina.project.model.Project;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectRepository extends MongoRepository<Project, String> {
  List<Project> findByClientId(String clientId);
}
