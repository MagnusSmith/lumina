package com.lumina.meter.dto;

import com.lumina.meter.model.Status;
import java.time.Instant;
import java.util.Map;

public record StatusDto(String id, String meterId, Instant timestamp, Map<String, Object> data) {

  public static StatusDto from(Status status) {
    return new StatusDto(status.id(), status.meterId(), status.timestamp(), status.data());
  }
}
