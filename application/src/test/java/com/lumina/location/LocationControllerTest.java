package com.lumina.location;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.location.dto.NewLocationDto;
import com.lumina.location.dto.UpdateLocationDto;
import com.lumina.location.model.Location;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LocationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LocationControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private LocationService locationService;

  @Test
  @DisplayName("POST /api/location should create a new location and return 201")
  void testCreateLocation() throws Exception {
    NewLocationDto newLocationDto = new NewLocationDto("project-1", "Test Location");
    Location savedLocation = new Location("location-1", "Test Location", "project-1", null);

    when(locationService.create(any(Location.class))).thenReturn(savedLocation);

    mockMvc
        .perform(
            post("/api/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newLocationDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("location-1"))
        .andExpect(jsonPath("$.name").value("Test Location"))
        .andExpect(jsonPath("$.projectId").value("project-1"));
  }

  @Test
  @DisplayName("PUT /api/location should update existing location and return 200")
  void testUpdateLocation() throws Exception {
    UpdateLocationDto updateLocationDto =
        new UpdateLocationDto("location-1", "project-1", "Updated Location");
    Location updatedLocation = new Location("location-1", "Updated Location", "project-1", null);

    when(locationService.update(any(Location.class))).thenReturn(updatedLocation);

    mockMvc
        .perform(
            put("/api/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateLocationDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("location-1"))
        .andExpect(jsonPath("$.name").value("Updated Location"))
        .andExpect(jsonPath("$.projectId").value("project-1"));
  }

  @Test
  @DisplayName("GET /api/location/{id} should return location when it exists")
  void testGetLocationById() throws Exception {
    Location location = new Location("location-1", "Test Location", "project-1", null);

    when(locationService.findById("location-1")).thenReturn(Optional.of(location));

    mockMvc
        .perform(get("/api/location/location-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("location-1"))
        .andExpect(jsonPath("$.name").value("Test Location"))
        .andExpect(jsonPath("$.projectId").value("project-1"));
  }

  @Test
  @DisplayName("GET /api/location/{id} should return 404 when location doesn't exist")
  void testGetLocationByIdNotFound() throws Exception {
    when(locationService.findById("non-existent")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/location/non-existent")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/location/project/{projectId} should return all locations for a project")
  void testGetLocationsByProjectId() throws Exception {
    Location location1 = new Location("location-1", "Location 1", "project-1", null);
    Location location2 = new Location("location-2", "Location 2", "project-1", null);

    when(locationService.findByProjectId("project-1")).thenReturn(List.of(location1, location2));

    mockMvc
        .perform(get("/api/location/project/project-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value("location-1"))
        .andExpect(jsonPath("$[0].name").value("Location 1"))
        .andExpect(jsonPath("$[0].projectId").value("project-1"))
        .andExpect(jsonPath("$[1].id").value("location-2"))
        .andExpect(jsonPath("$[1].name").value("Location 2"));
  }

  @Test
  @DisplayName("GET /api/location/project/{projectId} should return empty array when no locations")
  void testGetLocationsByProjectIdEmpty() throws Exception {
    when(locationService.findByProjectId("project-1")).thenReturn(List.of());

    mockMvc
        .perform(get("/api/location/project/project-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
