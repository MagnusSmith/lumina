package com.lumina.meter;

import com.lumina.meter.model.Meter;
import com.lumina.meter.model.info.MeterInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MeterController {

  private final MeterService meterService;

  public MeterController(MeterService meterService) {
    this.meterService = meterService;
  }

  @PostMapping("meter")
  @ResponseStatus(HttpStatus.CREATED)
  MeterInfo create(@RequestBody Meter meter) {
    return meterService.create(meter);
  }

  @GetMapping("meter/{id}")
  public ResponseEntity<MeterInfo> findById(@PathVariable String id){
    return meterService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @GetMapping("/meter/location/{locationId}")
  public List<MeterInfo> findByLocationId(@PathVariable String locationId){
    return meterService.findByLocationId(locationId);
  }
}
