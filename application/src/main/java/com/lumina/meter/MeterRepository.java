package com.lumina.meter;

import com.lumina.meter.model.Meter;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MeterRepository extends MongoRepository<Meter, String> {
  List<Meter> findByLocationId(String id);
}
