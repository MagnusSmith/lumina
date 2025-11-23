package com.lumina.meter;

import com.lumina.meter.model.Status;
import java.time.Instant;
import java.util.stream.Stream;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface StatusRepository extends MongoRepository<Status, String> {

  /**
   * Finds status records for a given meter within a time range. Returns a stream for efficient
   * processing of large datasets.
   *
   * @param meterId the meter ID
   * @param startTime the start of the time range (inclusive)
   * @param endTime the end of the time range (inclusive)
   * @return stream of status records
   */
  @Query("{ 'meterId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
  Stream<Status> findByMeterIdAndTimestampBetween(
      String meterId, Instant startTime, Instant endTime);

  /**
   * Finds the most recent status record for a given meter.
   *
   * @param meterId the meter ID
   * @return the most recent status record, or null if none exists
   */
  Status findFirstByMeterIdOrderByTimestampDesc(String meterId);
}
