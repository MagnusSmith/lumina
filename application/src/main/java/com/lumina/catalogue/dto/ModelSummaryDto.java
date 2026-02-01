package com.lumina.catalogue.dto;

import com.lumina.catalogue.model.CatalogueItem;

/** Simplified model information for dropdown selection. */
public record ModelSummaryDto(String model, String description) {
  public static ModelSummaryDto from(CatalogueItem item) {
    return new ModelSummaryDto(item.model(), item.description());
  }
}
