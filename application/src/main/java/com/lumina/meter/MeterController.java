package com.lumina.meter;

import com.lumina.meter.dto.MeterDto;
import com.lumina.meter.dto.NewMeterDto;
import com.lumina.meter.dto.UpdateMeterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
@Tag(name = "Meter", description = "Meter configuration and management APIs")
public class MeterController {

  private final MeterService meterService;

  public MeterController(MeterService meterService) {
    this.meterService = meterService;
  }

  @Operation(
      summary = "Create a new meter",
      description = "Creates a new meter with validation against catalogue constraints")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Meter created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
        @ApiResponse(responseCode = "404", description = "Catalogue model not found")
      })
  @PostMapping("meter")
  @ResponseStatus(HttpStatus.CREATED)
  MeterDto create(@RequestBody @Valid NewMeterDto newMeter) {
    var catalogueItem = meterService.findCatalogueItemByModel(newMeter.model());
    var meter = meterService.create(NewMeterDto.toModel(newMeter));
    return MeterDto.from(catalogueItem, meter, false);
  }

  @Operation(
      summary = "Update an existing meter",
      description = "Updates meter configuration with validation")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Meter updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
        @ApiResponse(responseCode = "404", description = "Meter or catalogue model not found")
      })
  @PutMapping("meter")
  MeterDto update(@RequestBody @Valid UpdateMeterDto updateMeter) {

    var meter = meterService.update(UpdateMeterDto.toModel(updateMeter));
    var catalogueItem = meterService.findCatalogueItemByModel(meter.model());
    return MeterDto.from(catalogueItem, meter, false);
  }

  @Operation(summary = "Get meter by ID", description = "Retrieves a meter configuration by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Meter found"),
        @ApiResponse(responseCode = "404", description = "Meter not found")
      })
  @GetMapping("meter/{id}")
  public ResponseEntity<MeterDto> findById(
      @PathVariable String id,
      @Parameter(description = "Include constraint details in response") @RequestParam
          Optional<Boolean> withConstraints) {
    return meterService
        .findById(id)
        .map(m -> meterService.toMeterDto(m, withConstraints.isPresent()))
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @Operation(
      summary = "Get meters by location",
      description = "Retrieves all meters for a specific location")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Meters retrieved successfully")})
  @GetMapping("meter/location/{locationId}")
  public List<MeterDto> findByLocationId(
      @PathVariable String locationId,
      @Parameter(description = "Include constraint details in response") @RequestParam
          Optional<Boolean> withConstraints) {
    return meterService.findByLocationId(locationId).stream()
        .map(m -> meterService.toMeterDto(m, withConstraints.isPresent()))
        .toList();
  }
}
