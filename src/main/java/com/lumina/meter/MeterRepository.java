package com.lumina.meter;


import com.lumina.meter.model.Meter;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MeterRepository extends MongoRepository<Meter, String> {
  List<Meter> findByLocationId(String id);
}
