package com.lumina.catalogue;

import com.lumina.NotFoundException;
import com.lumina.catalogue.model.CatalogueItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CatalogueService {

  private final ItemRepository catalogueRepository;


  public CatalogueService(ItemRepository catalogueRepository) {
    this.catalogueRepository = catalogueRepository;
  }

  public CatalogueItem create(CatalogueItem newItem) {
    return catalogueRepository.save(newItem);
  }

  public CatalogueItem update(CatalogueItem item) {
    var itemToUpdate =
        catalogueRepository
            .findById(item.id())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "The catalogue item with id %s could not be found!".formatted(item.id())));

   return catalogueRepository.save(item);
  }

  public void delete(String model) {
    catalogueRepository.deleteByModel(model);
  }

  public List<CatalogueItem> findAll() {
    return catalogueRepository.findAll();
  }

  public Optional<CatalogueItem> findByIdModel(String model) {
    return catalogueRepository.findByModel(model);
  }


}
