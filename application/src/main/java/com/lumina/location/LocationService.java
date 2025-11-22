package com.lumina.location;

import com.lumina.NotFoundException;
import com.lumina.location.model.Location;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

  private final LocationRepository repository;

  public LocationService(LocationRepository repository) {
    this.repository = repository;
  }

  public Location create(Location location) {
    return repository.save(location);
  }

  /**
   * Updates a location. This method validates that the location exists before performing the update.
   *
   * @param location the location with updated data
   * @return the updated location
   * @throws NotFoundException if the location does not exist
   */
  public Location update(Location location) {
    if (!repository.existsById(location.id())) {
      throw new NotFoundException(
          "The location with id %s could not be found!".formatted(location.id()));
    }
    return repository.save(location);
  }

  public Optional<Location> findById(String id) {
    return repository.findById(id);
  }
}
