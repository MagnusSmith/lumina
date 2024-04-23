package com.lumina.catalogue;

import com.lumina.catalogue.dto.CatalogueItemDto;
import com.lumina.catalogue.dto.NewCatalogueItemDto;
import com.lumina.catalogue.dto.UpdateCatalogueItemDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class CatalogueController {

  private final CatalogueService service;

  public CatalogueController(CatalogueService service) {
    this.service = service;
  }

  @PostMapping("catalogueItem")
  @ResponseStatus(HttpStatus.CREATED)
  public CatalogueItemDto create(@Valid @RequestBody NewCatalogueItemDto item) {

    return CatalogueItemDto.from(service.create(NewCatalogueItemDto.toModel(item)));
  }

  @PutMapping("catalogueItem")
  public CatalogueItemDto update(@Valid @RequestBody UpdateCatalogueItemDto item) {

    return CatalogueItemDto.from(service.update(UpdateCatalogueItemDto.toModel(item)));
  }

  @DeleteMapping("catalogueItem/{model}")
  public void delete(@PathVariable String model) {
    service.delete(model);
  }

  @GetMapping("catalogueItems")
  public List<CatalogueItemDto> getItems() {
    return service.findAll().stream().map(CatalogueItemDto::from).toList();
  }

  @GetMapping("catalogueItem/{model}")
  public ResponseEntity<CatalogueItemDto> getItem(@PathVariable String model) {
    return service
        .findByModel(model)
        .map(CatalogueItemDto::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }
}
