package com.lumina.project;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.project.dto.NewProjectDto;
import com.lumina.project.dto.UpdateProjectDto;
import com.lumina.project.model.Project;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProjectControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ProjectService projectService;

  @Test
  @DisplayName("POST /api/project should create a new project and return 201")
  void testCreateProject() throws Exception {
    NewProjectDto newProjectDto = new NewProjectDto("client-1", "Test Project");
    Project savedProject = new Project("project-1", "client-1", "Test Project", null, null);

    when(projectService.create(any(Project.class))).thenReturn(savedProject);

    mockMvc
        .perform(
            post("/api/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProjectDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("project-1"))
        .andExpect(jsonPath("$.name").value("Test Project"))
        .andExpect(jsonPath("$.clientId").value("client-1"));
  }

  @Test
  @DisplayName("PUT /api/project should update existing project and return 200")
  void testUpdateProject() throws Exception {
    UpdateProjectDto updateProjectDto =
        new UpdateProjectDto("project-1", "client-1", "Updated Project", "BillingGroup1");
    Project updatedProject =
        new Project("project-1", "client-1", "Updated Project", "BillingGroup1", null);

    when(projectService.update(any(Project.class))).thenReturn(updatedProject);

    mockMvc
        .perform(
            put("/api/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProjectDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("project-1"))
        .andExpect(jsonPath("$.name").value("Updated Project"))
        .andExpect(jsonPath("$.clientId").value("client-1"))
        .andExpect(jsonPath("$.billingGroup").value("BillingGroup1"));
  }

  @Test
  @DisplayName("GET /api/project/client/{clientId} should return all projects for a client")
  void testGetProjectsByClientId() throws Exception {
    Project project1 = new Project("project-1", "client-1", "Project 1", null, List.of());
    Project project2 = new Project("project-2", "client-1", "Project 2", "Billing1", List.of());

    when(projectService.findByClientId("client-1")).thenReturn(List.of(project1, project2));

    mockMvc
        .perform(get("/api/project/client/client-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value("project-1"))
        .andExpect(jsonPath("$[0].name").value("Project 1"))
        .andExpect(jsonPath("$[0].clientId").value("client-1"))
        .andExpect(jsonPath("$[1].id").value("project-2"))
        .andExpect(jsonPath("$[1].name").value("Project 2"));
  }

  @Test
  @DisplayName("GET /api/project/client/{clientId} should return empty array when no projects")
  void testGetProjectsByClientIdEmpty() throws Exception {
    when(projectService.findByClientId("client-1")).thenReturn(List.of());

    mockMvc
        .perform(get("/api/project/client/client-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
