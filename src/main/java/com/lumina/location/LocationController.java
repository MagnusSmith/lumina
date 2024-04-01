package com.lumina.location;


import com.lumina.location.model.Location;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LocationController {
  private final LocationService locationService;

  public LocationController(LocationService locationService) {
    this.locationService = locationService;
  }

  @GetMapping("location/{id}")
  public ResponseEntity<Location> findById(@PathVariable String id) {
    return locationService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @PostMapping("location")
  @ResponseStatus(HttpStatus.CREATED)
  public Location create(@RequestBody Location newLocation) {
    return locationService.create(newLocation);
  }

  @PutMapping("location")
  public Location update(@RequestBody Location updateLocation) {
    return locationService.update(updateLocation);
  }
}
