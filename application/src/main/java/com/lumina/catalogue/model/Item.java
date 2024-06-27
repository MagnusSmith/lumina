package com.lumina.catalogue.model;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "catalogue")
@TypeAlias("Item")
public sealed interface Item permits Preset, CatalogueItem {
    String id();
}
