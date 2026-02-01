package com.lumina.location;

import com.lumina.location.dto.LocationShortDto;
import com.lumina.location.dto.NewLocationDto;
import com.lumina.location.dto.UpdateLocationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
@Tag(name = "Location", description = "Location management APIs")
public class LocationController {
  private final LocationService locationService;

  public LocationController(LocationService locationService) {
    this.locationService = locationService;
  }

  @GetMapping("location/{id}")
  @Operation(summary = "Get location by ID")
  @ApiResponse(responseCode = "200", description = "Location found")
  @ApiResponse(responseCode = "404", description = "Location not found")
  public ResponseEntity<LocationShortDto> findById(
      @Parameter(description = "Location ID") @PathVariable String id) {
    return locationService
        .findById(id)
        .map(LocationShortDto::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @GetMapping("location/project/{projectId}")
  @Operation(summary = "Get all locations for a project")
  @ApiResponse(responseCode = "200", description = "Locations retrieved successfully")
  public List<LocationShortDto> getByProjectId(
      @Parameter(description = "Project ID") @PathVariable String projectId) {
    return locationService.findByProjectId(projectId).stream().map(LocationShortDto::from).toList();
  }

  @PostMapping("location")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new location")
  @ApiResponse(responseCode = "201", description = "Location created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input")
  public LocationShortDto create(@RequestBody @Valid NewLocationDto newLocation) {
    var location = locationService.create(NewLocationDto.toModel(newLocation));
    return LocationShortDto.from(location);
  }

  @PutMapping("location")
  @Operation(summary = "Update an existing location")
  @ApiResponse(responseCode = "200", description = "Location updated successfully")
  @ApiResponse(responseCode = "400", description = "Invalid input")
  @ApiResponse(responseCode = "404", description = "Location not found")
  public LocationShortDto update(@RequestBody @Valid UpdateLocationDto updateLocation) {
    var location = locationService.update(UpdateLocationDto.toModel(updateLocation));
    return LocationShortDto.from(location);
  }

  @DeleteMapping("location/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete a location")
  @ApiResponse(responseCode = "204", description = "Location deleted successfully")
  @ApiResponse(responseCode = "404", description = "Location not found")
  public void delete(@Parameter(description = "Location ID") @PathVariable String id) {
    locationService.delete(id);
  }
}
