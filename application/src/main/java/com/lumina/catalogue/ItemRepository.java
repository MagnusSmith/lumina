package com.lumina.catalogue;

import com.lumina.catalogue.model.*;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ItemRepository extends MongoRepository<Item, String> {

    @Query(value = "{ '_class' : 'CatalogueItem' }")
    List<CatalogueItem> findAllCatalogueItems();

    @Query(value = "{ '_class' : 'CatalogueItem', 'model' : ?0 }")
    Optional<CatalogueItem> findByModel(String model);

    @Query(value = "{ '_class' : 'CatalogueItem', 'model' : ?0 }")
    void deleteByModel(String model);

    @Query(value = "{ '_class' : 'Preset', 'type' : ?0, 'level' : ?1 }")
    Optional<Preset> findByTypeAndLevel(MeterType type, Level level);
}
