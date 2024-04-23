package com.lumina.catalogue;

import com.lumina.catalogue.model.CatalogueItem;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<CatalogueItem, String> {
  boolean existsByModel(String model);

  Optional<CatalogueItem> findByModel(String model);

  void deleteByModel(String model);
}
