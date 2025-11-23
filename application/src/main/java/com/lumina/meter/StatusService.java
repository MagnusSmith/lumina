package com.lumina.meter;

import com.lumina.meter.model.Status;
import com.lumina.meter.model.StatusBuilder;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusService {

  private final StatusRepository statusRepository;
  private final MeterRepository meterRepository;

  public StatusService(StatusRepository statusRepository, MeterRepository meterRepository) {
    this.statusRepository = statusRepository;
    this.meterRepository = meterRepository;
  }

  /**
   * Creates a new status record for a meter.
   *
   * @param meterId the meter ID
   * @param data the status data
   * @return the created status record
   */
  public Status createStatus(String meterId, Map<String, Object> data) {
    // Verify meter exists
    if (!meterRepository.existsById(meterId)) {
      throw new IllegalArgumentException("Meter with id %s does not exist".formatted(meterId));
    }

    Status status =
        StatusBuilder.builder().meterId(meterId).timestamp(Instant.now()).data(data).build();

    return statusRepository.save(status);
  }

  /**
   * Retrieves status records for a meter within a time range. Uses streaming for efficient
   * processing of large datasets.
   *
   * @param meterId the meter ID
   * @param startTime the start of the time range
   * @param endTime the end of the time range
   * @return stream of status records
   */
  @Transactional(readOnly = true)
  public Stream<Status> getStatusStream(String meterId, Instant startTime, Instant endTime) {
    return statusRepository.findByMeterIdAndTimestampBetween(meterId, startTime, endTime);
  }

  /**
   * Retrieves the most recent status record for a meter.
   *
   * @param meterId the meter ID
   * @return optional containing the most recent status, or empty if none exists
   */
  public Optional<Status> getLatestStatus(String meterId) {
    return Optional.ofNullable(statusRepository.findFirstByMeterIdOrderByTimestampDesc(meterId));
  }
}
