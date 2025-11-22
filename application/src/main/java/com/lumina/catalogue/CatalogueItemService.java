package com.lumina.catalogue;

import com.lumina.NotFoundException;
import com.lumina.catalogue.model.CatalogueItem;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CatalogueItemService {

  private final ItemRepository itemRepository;

  public CatalogueItemService(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  public CatalogueItem create(CatalogueItem newItem) {
    return itemRepository.save(newItem);
  }

  /**
   * Updates a catalogue item. This method validates that the item exists before performing the
   * update.
   *
   * @param item the catalogue item with updated data
   * @return the updated catalogue item
   * @throws NotFoundException if the catalogue item does not exist
   */
  public CatalogueItem update(CatalogueItem item) {
    Objects.requireNonNull(item.id());
    if (!itemRepository.existsById(item.id())) {
      throw new NotFoundException(
          "The catalogue item with id %s could not be found!".formatted(item.id()));
    }
    return itemRepository.save(item);
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
