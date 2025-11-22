package com.lumina.catalogue;

import com.lumina.catalogue.dto.CatalogueItemDto;
import com.lumina.catalogue.dto.NewCatalogueItemDto;
import com.lumina.catalogue.dto.PresetDto;
import com.lumina.catalogue.dto.UpdateCatalogueItemDto;
import com.lumina.catalogue.model.CatalogueItemBuilder;
import com.lumina.catalogue.model.Level;
import com.lumina.catalogue.model.MeterType;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/catalogue/")
public class CatalogueController {

  private final CatalogueItemService itemService;
  private final CataloguePresetService presetService;

  public CatalogueController(
      CatalogueItemService itemService, CataloguePresetService presetService) {
    this.itemService = itemService;
    this.presetService = presetService;
  }

  @PostMapping("item")
  @ResponseStatus(HttpStatus.CREATED)
  public CatalogueItemDto create(@Valid @RequestBody NewCatalogueItemDto newItem) {
    var item = NewCatalogueItemDto.toModel(newItem);
    var presetItem =
        presetService
            .findByTypeAndLevel(item.type(), item.level())
            .map(
                preset ->
                    CatalogueItemBuilder.from(item)
                        .with()
                        .constraints(preset.constraints())
                        .lines(preset.lines())
                        .build())
            .orElse(item);

    return CatalogueItemDto.from(itemService.create(presetItem));
  }

  @PutMapping("item")
  public CatalogueItemDto update(@Valid @RequestBody UpdateCatalogueItemDto item) {

    return CatalogueItemDto.from(itemService.update(UpdateCatalogueItemDto.toModel(item)));
  }

  @DeleteMapping("item/{model}")
  public void delete(@PathVariable String model) {
    itemService.delete(model);
  }

  @GetMapping("items")
  public List<CatalogueItemDto> getItems() {
    return itemService.findAll().stream().map(CatalogueItemDto::from).toList();
  }

  @GetMapping("item/{model}")
  public ResponseEntity<CatalogueItemDto> getItem(@PathVariable String model) {
    return itemService
        .findByModel(model)
        .map(CatalogueItemDto::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @PostMapping("preset")
  @ResponseStatus(HttpStatus.CREATED)
  public PresetDto.Info create(@Valid @RequestBody PresetDto.New newPreset) {

    return PresetDto.from(presetService.create(PresetDto.toModel(newPreset)));
  }

  @PutMapping("preset")
  public PresetDto.Info update(@Valid @RequestBody PresetDto.Update updatePreset) {
    return PresetDto.from(presetService.update(PresetDto.toModel(updatePreset)));
  }

  @GetMapping("preset/{type}/{level}")
  public ResponseEntity<PresetDto.Info> getPreset(
      @PathVariable MeterType type, @PathVariable Level level) {
    return presetService
        .findByTypeAndLevel(type, level)
        .map(PresetDto::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }
}
