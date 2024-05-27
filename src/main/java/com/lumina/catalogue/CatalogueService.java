package com.lumina.catalogue;

import com.lumina.NotFoundException;
import com.lumina.catalogue.model.CatalogueItem;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.lumina.catalogue.model.Level;
import com.lumina.catalogue.model.MeterType;
import com.lumina.catalogue.model.Preset;
import org.springframework.stereotype.Service;


public class CatalogueService {

  @Service
  public static class Item {

  private final ItemRepository itemRepository;



  public Item(ItemRepository itemRepository) {

    this.itemRepository = itemRepository;
  }

  public CatalogueItem create(CatalogueItem newItem) {
    return itemRepository.save(newItem);
  }

  public CatalogueItem update(CatalogueItem item) {
    Objects.requireNonNull(item.id());
    var itemToUpdate =
        itemRepository
            .findById(item.id())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "The catalogue item with id %s could not be found!".formatted(item.id())));

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
  @Service
  public static class PresetService {
    private final ItemRepository presetRepository;

    public PresetService(ItemRepository presetRepository) {
      this.presetRepository = presetRepository;
    }

    public Preset create(Preset preset){
      presetRepository.findByTypeAndLevel(preset.type(), preset.level())
          .ifPresent(p -> { throw new RuntimeException("The Preset %s already exists.".formatted(p)); });
      return presetRepository.insert(preset);
    }

    public  Preset update(Preset preset){
      return presetRepository.save(preset);
    }

    public  Optional<Preset> findByTypeAndLevel(MeterType type, Level level){
      return  presetRepository.findByTypeAndLevel(type, level);
    }


  }


}
