package com.lumina.catalogue;

import com.lumina.DuplicateResourceException;
import com.lumina.catalogue.model.Level;
import com.lumina.catalogue.model.MeterType;
import com.lumina.catalogue.model.Preset;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CataloguePresetService {

  private final ItemRepository presetRepository;

  public CataloguePresetService(ItemRepository presetRepository) {
    this.presetRepository = presetRepository;
  }

  /**
   * Creates a new preset. Validates that a preset for the given type and level doesn't already
   * exist.
   *
   * @param preset the preset to create
   * @return the created preset
   * @throws DuplicateResourceException if a preset for the type and level already exists
   */
  public Preset create(Preset preset) {
    presetRepository
        .findByTypeAndLevel(preset.type(), preset.level())
        .ifPresent(
            p -> {
              throw new DuplicateResourceException(
                  "A preset for type %s and level %s already exists."
                      .formatted(preset.type(), preset.level()));
            });
    return presetRepository.insert(preset);
  }

  public Preset update(Preset preset) {
    return presetRepository.save(preset);
  }

  public Optional<Preset> findByTypeAndLevel(MeterType type, Level level) {
    return presetRepository.findByTypeAndLevel(type, level);
  }
}
