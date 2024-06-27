package com.lumina.meter;

import com.lumina.meter.dto.MeterDto;
import com.lumina.meter.dto.NewMeterDto;
import com.lumina.meter.dto.UpdateMeterDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class MeterController {

  private final MeterService meterService;

  public MeterController(MeterService meterService) {
    this.meterService = meterService;
  }

  @PostMapping("meter")
  @ResponseStatus(HttpStatus.CREATED)
  MeterDto create(@RequestBody @Valid NewMeterDto newMeter) {
    var catalogueItem = meterService.findCatalogueItemByModel(newMeter.model());
    var meter = meterService.create(NewMeterDto.toModel(newMeter));
    return MeterDto.from(catalogueItem, meter, false);
  }

  @PutMapping("meter")
  MeterDto update(@RequestBody UpdateMeterDto updateMeter) {

    var meter = meterService.update(UpdateMeterDto.toModel(updateMeter));
    var catalogueItem = meterService.findCatalogueItemByModel(meter.model());
    return MeterDto.from(catalogueItem, meter, false);
  }

  @GetMapping("meter/{id}")
  public ResponseEntity<MeterDto> findById(
      @PathVariable String id, @RequestParam Optional<Boolean> withConstraints) {
    return meterService
        .findById(id)
        .map(m -> meterService.toMeterDto(m, withConstraints.isPresent()))
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @GetMapping("meter/location/{locationId}")
  public List<MeterDto> findByLocationId(
      @PathVariable String locationId, @RequestParam Optional<Boolean> withConstraints) {
    return meterService.findByLocationId(locationId).stream()
        .map(m -> meterService.toMeterDto(m, withConstraints.isPresent()))
        .toList();
  }
}
