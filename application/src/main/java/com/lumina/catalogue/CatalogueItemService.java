package com.lumina.catalogue;

import com.lumina.NotFoundException;
import com.lumina.catalogue.model.CatalogueItem;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class CatalogueItemService {

  private final ItemRepository itemRepository;
  private final MongoTemplate mongoTemplate;

  public CatalogueItemService(ItemRepository itemRepository, MongoTemplate mongoTemplate) {
    this.itemRepository = itemRepository;
    this.mongoTemplate = mongoTemplate;
  }

  public CatalogueItem create(CatalogueItem newItem) {
    return itemRepository.save(newItem);
  }

  /**
   * Updates a catalogue item. This method validates that the item exists before performing the
   * update. Uses MongoTemplate to perform an explicit replace operation.
   *
   * @param item the catalogue item with updated data
   * @return the updated catalogue item
   * @throws NotFoundException if the catalogue item does not exist
   */
  public CatalogueItem update(CatalogueItem item) {
    Objects.requireNonNull(item.id());

    // Verify item exists
    if (!itemRepository.existsById(item.id())) {
      throw new NotFoundException(
          "The catalogue item with id %s could not be found!".formatted(item.id()));
    }

    // Delete and re-insert to avoid save() confusion between insert and update
    Query query = new Query(Criteria.where("_id").is(item.id()));
    mongoTemplate.remove(query, CatalogueItem.class);
    return mongoTemplate.insert(item);
  }

  public void delete(String model) {
    itemRepository.deleteByModel(model);
  }

  public List<CatalogueItem> findAll() {
    return itemRepository.findAllCatalogueItems();
  }

  public Optional<CatalogueItem> findByModel(String model) {
    return itemRepository.findByModel(model);
  }
}
