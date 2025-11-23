package com.lumina.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lumina.NotFoundException;
import com.lumina.project.model.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

  @Mock private ProjectRepository repository;

  @InjectMocks private ProjectService projectService;

  private Project testProject;

  @BeforeEach
  void setup() {
    testProject = new Project("project-1", "client-1", "Test Project", "BillingGroup1", null);
  }

  @Test
  @DisplayName("create() should save and return a new project")
  void testCreate() {
    when(repository.save(any(Project.class))).thenReturn(testProject);

    Project result = projectService.create(testProject);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("project-1");
    assertThat(result.name()).isEqualTo("Test Project");
    assertThat(result.clientId()).isEqualTo("client-1");
    verify(repository).save(testProject);
  }

  @Test
  @DisplayName("update() should update and return existing project")
  void testUpdate() {
    when(repository.existsById("project-1")).thenReturn(true);
    when(repository.save(any(Project.class))).thenReturn(testProject);

    Project result = projectService.update(testProject);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("project-1");
    verify(repository).existsById("project-1");
    verify(repository).save(testProject);
  }

  @Test
  @DisplayName("update() should throw NotFoundException when project doesn't exist")
  void testUpdateNotFound() {
    when(repository.existsById("non-existent")).thenReturn(false);

    Project nonExistentProject =
        new Project("non-existent", "client-1", "Non Existent", "BillingGroup1", null);

    assertThatThrownBy(() -> projectService.update(nonExistentProject))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("The project with id non-existent could not be found!");

    verify(repository).existsById("non-existent");
  }
}
