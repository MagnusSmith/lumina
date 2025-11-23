package com.lumina.location;

import com.lumina.location.model.Location;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationRepository extends MongoRepository<Location, String> {
  List<Location> findByProjectId(String projectId);
}
