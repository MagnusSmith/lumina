package com.lumina.location;

import com.lumina.location.model.Location;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocationService {

  private final LocationRepository repository;

  public LocationService(LocationRepository repository) {
    this.repository = repository;
  }

  public Location create(Location location) {
    return repository.save(location);
  }

  public Location update(Location location) {
    return repository.save(location);
  }

  public Optional<Location> findById(String id) {
    return repository.findById(id);
  }
}
