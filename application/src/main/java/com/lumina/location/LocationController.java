package com.lumina.location;

import com.lumina.location.dto.LocationShortDto;
import com.lumina.location.dto.NewLocationDto;
import com.lumina.location.dto.UpdateLocationDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class LocationController {
  private final LocationService locationService;

  public LocationController(LocationService locationService) {
    this.locationService = locationService;
  }

  @GetMapping("location/{id}")
  public ResponseEntity<LocationShortDto> findById(@PathVariable String id) {
    return locationService
        .findById(id)
        .map(LocationShortDto::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @PostMapping("location")
  @ResponseStatus(HttpStatus.CREATED)
  public LocationShortDto create(@RequestBody @Valid NewLocationDto newLocation) {

    var location = locationService.create(NewLocationDto.toModel(newLocation));
    return LocationShortDto.from(location);
  }

  @PutMapping("location")
  public LocationShortDto update(@RequestBody @Valid UpdateLocationDto updateLocation) {
    var location = locationService.update(UpdateLocationDto.toModel(updateLocation));
    return LocationShortDto.from(location);
  }
}
