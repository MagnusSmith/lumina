package com.lumina.meter.dto;

import com.lumina.catalogue.model.ValidationStage;
import com.lumina.client.model.Client;
import com.lumina.location.model.Location;
import com.lumina.meter.model.Line;
import com.lumina.meter.model.Meter;
import com.lumina.project.model.Project;
import java.util.List;

/**
 * DTO for displaying meters in the web UI with full location hierarchy.
 *
 * <p>Includes client, project, and location names for display instead of just IDs.
 */
public record MeterViewDto(
    String id,
    String model,
    String locationId,
    String locationName,
    String projectId,
    String projectName,
    String clientId,
    String clientName,
    List<Line> lines,
    ValidationStage stage) {

  public static MeterViewDto from(Meter meter, Location location, Project project, Client client) {
    return new MeterViewDto(
        meter.id(),
        meter.model(),
        meter.locationId(),
        location.name(),
        project.id(),
        project.name(),
        client.id(),
        client.name(),
        meter.lines(),
        meter.stage());
  }

  /** Returns formatted location breadcrumb: "Client / Project / Location" */
  public String locationBreadcrumb() {
    return clientName + " / " + projectName + " / " + locationName;
  }
}
