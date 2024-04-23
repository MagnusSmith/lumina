package com.lumina.meter;

import com.lumina.meter.dto.MeterDto;
import com.lumina.meter.dto.NewMeterDto;
import com.lumina.meter.dto.UpdateMeterDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MeterController {

  private final MeterService meterService;

  public MeterController(MeterService meterService) {
    this.meterService = meterService;
  }

  @PostMapping("meter")
  @ResponseStatus(HttpStatus.CREATED)
  MeterDto create(@RequestBody NewMeterDto newMeter) {

    var meter = meterService.create(NewMeterDto.toModel(newMeter));
    var catalogueItem = meterService.findCatalogueItemByModel(meter.model());
    return MeterDto.from(catalogueItem, meter);
  }


  @PutMapping("meter")
  MeterDto update(@RequestBody UpdateMeterDto updateMeter) {

    var meter = meterService.update(UpdateMeterDto.toModel(updateMeter));
    var catalogueItem = meterService.findCatalogueItemByModel(meter.model());
    return MeterDto.from(catalogueItem, meter);
  }

  @GetMapping("meter/{id}")
  public ResponseEntity<MeterDto> findById(@PathVariable String id){
    return meterService
        .findById(id)
        .map(meterService::toMeterDto)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @GetMapping("/meter/location/{locationId}")
  public List<MeterDto> findByLocationId(@PathVariable String locationId){
    return meterService.findByLocationId(locationId).stream()
        .map(meterService::toMeterDto).toList();
  }


}
