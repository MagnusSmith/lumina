package com.lumina.meter;

import com.lumina.meter.dto.StatusDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meter/status")
@Tag(name = "Meter Status", description = "Meter status data APIs")
public class StatusController {

  private final StatusService statusService;

  public StatusController(StatusService statusService) {
    this.statusService = statusService;
  }

  @Operation(
      summary = "Get status records for a meter",
      description =
          "Retrieves status records for a specific meter within a time range. Returns a stream of status data.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Status records retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid time range parameters")
      })
  @GetMapping("/{meterId}")
  public List<StatusDto> getStatusByTimeRange(
      @Parameter(description = "Meter ID") @PathVariable String meterId,
      @Parameter(description = "Start time (ISO-8601 format)", example = "2025-11-23T00:00:00Z")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant startTime,
      @Parameter(description = "End time (ISO-8601 format)", example = "2025-11-23T23:59:59Z")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant endTime) {

    if (startTime.isAfter(endTime)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }

    // Convert stream to list for JSON serialization
    // For large datasets, consider pagination or server-sent events
    try (var stream = statusService.getStatusStream(meterId, startTime, endTime)) {
      return stream.map(StatusDto::from).toList();
    }
  }

  @Operation(
      summary = "Get latest status for a meter",
      description = "Retrieves the most recent status record for a specific meter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Latest status retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "No status records found for this meter")
      })
  @GetMapping("/{meterId}/latest")
  public ResponseEntity<StatusDto> getLatestStatus(
      @Parameter(description = "Meter ID") @PathVariable String meterId) {
    return statusService
        .getLatestStatus(meterId)
        .map(StatusDto::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }
}
