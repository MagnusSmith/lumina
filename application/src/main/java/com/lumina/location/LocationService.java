package com.lumina.location;

import com.lumina.NotFoundException;
import com.lumina.location.model.Location;
import com.lumina.meter.MeterRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

  private final LocationRepository repository;
  private final MeterRepository meterRepository;

  public LocationService(LocationRepository repository, MeterRepository meterRepository) {
    this.repository = repository;
    this.meterRepository = meterRepository;
  }

  public Location create(Location location) {
    return repository.save(location);
  }

  /**
   * Updates a location. This method validates that the location exists before performing the
   * update.
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

  public List<Location> findAllById(Collection<String> ids) {
    return repository.findAllById(ids);
  }

  public List<Location> findByProjectId(String projectId) {
    return repository.findByProjectId(projectId);
  }

  public List<Location> findAll() {
    return repository.findAll();
  }

  /**
   * Deletes a location. This method validates that no meters exist at the location before
   * performing the delete.
   *
   * @param id the location ID to delete
   * @throws NotFoundException if the location does not exist
   * @throws IllegalStateException if meters exist at the location
   */
  public void delete(String id) {
    // Verify location exists
    if (!repository.existsById(id)) {
      throw new NotFoundException("The location with id %s could not be found!".formatted(id));
    }

    // Check if any meters exist at this location
    List<com.lumina.meter.model.Meter> meters = meterRepository.findByLocationId(id);
    if (!meters.isEmpty()) {
      throw new IllegalStateException(
          "Cannot delete location: %d meter(s) still exist at this location. Please delete all meters first."
              .formatted(meters.size()));
    }

    repository.deleteById(id);
  }
}
