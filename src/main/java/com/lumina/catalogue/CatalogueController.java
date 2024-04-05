package com.lumina.catalogue;

import com.lumina.catalogue.model.CatalogueItem;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
  public CatalogueItem create(@Valid @RequestBody CatalogueItem item) {
    return service.create(item);
  }

  @PutMapping("catalogueItem")
  // @TODO: need to check id is set
  public CatalogueItem update(@Valid @RequestBody CatalogueItem item) {
    return service.update(item);
  }

  @DeleteMapping("catalogueItem/{model}")
  public void delete(@PathVariable String model) {
    service.delete(model);
  }

  @GetMapping("catalogueItems")
  public List<CatalogueItem> getItems() {
    return service.findAll();
  }

  @GetMapping("catalogueItem/{model}")
  public ResponseEntity<CatalogueItem> getItem(@PathVariable String model) {
    return service
        .findByIdModel(model)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }
}
