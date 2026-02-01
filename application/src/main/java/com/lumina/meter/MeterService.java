package com.lumina.meter;

import com.lumina.NotFoundException;
import com.lumina.catalogue.ItemRepository;
import com.lumina.catalogue.model.CatalogueItem;
import com.lumina.client.ClientService;
import com.lumina.client.model.Client;
import com.lumina.location.model.Location;
import com.lumina.location.LocationService;
import com.lumina.meter.dto.MeterDto;
import com.lumina.meter.dto.MeterViewDto;
import com.lumina.meter.model.Meter;
import com.lumina.meter.validation.MeterValidator;
import com.lumina.project.ProjectService;
import com.lumina.project.model.Project;
import com.lumina.validation.Errors;
import com.lumina.validation.LuminaValidationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MeterService {

  private final MeterRepository repository;
  private final ItemRepository catalogueItemRepository;
  private final MeterValidator meterValidator;
  private final LocationService locationService;
  private final ProjectService projectService;
  private final ClientService clientService;

  public MeterService(
      MeterRepository repository,
      ItemRepository catalogueItemRepository,
      MeterValidator meterValidator,
      LocationService locationService,
      ProjectService projectService,
      ClientService clientService) {
    this.repository = repository;
    this.catalogueItemRepository = catalogueItemRepository;
    this.meterValidator = meterValidator;
    this.locationService = locationService;
    this.projectService = projectService;
    this.clientService = clientService;
  }

  public Meter create(Meter meter) {
    Errors errors = new Errors("meter");
    meterValidator.validate(meter, errors);

    if (errors.getErrorCount() > 0) {
      throw new LuminaValidationException(errors);
    }

    return repository.save(meter);
  }

  public Meter update(Meter meter) {
    Errors errors = new Errors("meter");
    meterValidator.validate(meter, errors);

    if (errors.getErrorCount() > 0) {
      throw new LuminaValidationException(errors);
    }
    var meterToUpdate =
        repository
            .findById(meter.id())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "The meter with id %s could not be found!".formatted(meter.id())));

    return repository.save(meter);
  }

  public CatalogueItem findCatalogueItemByModel(String model) {
    return catalogueItemRepository
        .findByModel(model)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "The meter model %s could not be found in the catalogue!".formatted(model)));
  }

  public Optional<Meter> findById(String id) {
    return repository.findById(id);
  }

  public List<Meter> findByLocationId(String locationId) {
    return repository.findByLocationId(locationId);
  }

  public List<Meter> findAll() {
    return repository.findAll();
  }

  public void delete(String id) {
    if (!repository.existsById(id)) {
      throw new NotFoundException("The meter with id %s could not be found!".formatted(id));
    }
    repository.deleteById(id);
  }

  MeterDto toMeterDto(Meter meter, boolean withConstraints) {
    var catItem = findCatalogueItemByModel(meter.model());
    return MeterDto.from(catItem, meter, withConstraints);
  }

  /**
   * Converts a Meter to MeterViewDto with full location hierarchy (client/project/location names).
   */
  public MeterViewDto toMeterViewDto(Meter meter) {
    var location =
        locationService
            .findById(meter.locationId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Location %s not found for meter %s"
                            .formatted(meter.locationId(), meter.id())));

    var project =
        projectService
            .findById(location.projectId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Project %s not found for location %s"
                            .formatted(location.projectId(), location.id())));

    var client =
        clientService
            .findById(project.clientId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Client %s not found for project %s"
                            .formatted(project.clientId(), project.id())));

    return MeterViewDto.from(meter, location, project, client);
  }

  /**
   * Returns all meters enriched with location hierarchy for web display.
   * Uses batch fetching to avoid N+1 query problem - fetches all related
   * entities in 4 queries total instead of 1 + 3*N queries.
   */
  public List<MeterViewDto> findAllForView() {
    List<Meter> meters = repository.findAll();
    if (meters.isEmpty()) {
      return List.of();
    }

    // Batch fetch all locations (1 query)
    Map<String, Location> locationMap =
        locationService
            .findAllById(meters.stream().map(Meter::locationId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(Location::id, Function.identity()));

    // Batch fetch all projects (1 query)
    Map<String, Project> projectMap =
        projectService
            .findAllById(
                locationMap.values().stream().map(Location::projectId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(Project::id, Function.identity()));

    // Batch fetch all clients (1 query)
    Map<String, Client> clientMap =
        clientService
            .findAllById(
                projectMap.values().stream().map(Project::clientId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(Client::id, Function.identity()));

    // Build DTOs using the maps
    return meters.stream()
        .map(
            meter -> {
              Location location = locationMap.get(meter.locationId());
              Project project = projectMap.get(location.projectId());
              Client client = clientMap.get(project.clientId());
              return MeterViewDto.from(meter, location, project, client);
            })
        .toList();
  }
}
