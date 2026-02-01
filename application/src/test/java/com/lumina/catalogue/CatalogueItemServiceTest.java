package com.lumina.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lumina.NotFoundException;
import com.lumina.catalogue.model.CatalogueItem;
import com.lumina.catalogue.model.Level;
import com.lumina.catalogue.model.MeterType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@ExtendWith(MockitoExtension.class)
public class CatalogueItemServiceTest {

  @Mock private ItemRepository itemRepository;

  @Mock private MongoTemplate mongoTemplate;

  private CatalogueItemService catalogueItemService;

  private CatalogueItem testItem;

  @BeforeEach
  void setup() {
    catalogueItemService = new CatalogueItemService(itemRepository, mongoTemplate);
    testItem =
        new CatalogueItem(
            "item-1",
            "MODEL-001",
            Level.DEVICE,
            MeterType.LORAWAN,
            "Test Item",
            "Manufacturer A",
            List.of(),
            List.of());
  }

  @Test
  @DisplayName("create() should save and return a new catalogue item")
  void testCreate() {
    when(itemRepository.save(any(CatalogueItem.class))).thenReturn(testItem);

    CatalogueItem result = catalogueItemService.create(testItem);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("item-1");
    assertThat(result.model()).isEqualTo("MODEL-001");
    verify(itemRepository).save(testItem);
  }

  @Test
  @DisplayName("update() should update and return existing catalogue item")
  void testUpdate() {
    // findAndReplace returns the OLD document when found
    when(mongoTemplate.findAndReplace(any(Query.class), eq(testItem))).thenReturn(testItem);

    CatalogueItem result = catalogueItemService.update(testItem);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("item-1");
    verify(mongoTemplate).findAndReplace(any(Query.class), eq(testItem));
  }

  @Test
  @DisplayName("update() should throw NotFoundException when item doesn't exist")
  void testUpdateNotFound() {
    CatalogueItem nonExistentItem =
        new CatalogueItem(
            "non-existent",
            "MODEL-999",
            Level.DEVICE,
            MeterType.LORAWAN,
            "Non Existent",
            "Manufacturer X",
            List.of(),
            List.of());

    // findAndReplace returns null when document not found
    when(mongoTemplate.findAndReplace(any(Query.class), eq(nonExistentItem))).thenReturn(null);

    assertThatThrownBy(() -> catalogueItemService.update(nonExistentItem))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("The catalogue item with id non-existent could not be found!");

    verify(mongoTemplate).findAndReplace(any(Query.class), eq(nonExistentItem));
  }

  @Test
  @DisplayName("delete() should delete item by model")
  void testDelete() {
    catalogueItemService.delete("MODEL-001");

    verify(itemRepository).deleteByModel("MODEL-001");
  }

  @Test
  @DisplayName("findAll() should return all catalogue items")
  void testFindAll() {
    CatalogueItem item2 =
        new CatalogueItem(
            "item-2",
            "MODEL-002",
            Level.GATEWAY,
            MeterType.MODBUS,
            "Test Item 2",
            "Manufacturer B",
            List.of(),
            List.of());

    when(itemRepository.findAllCatalogueItems()).thenReturn(List.of(testItem, item2));

    List<CatalogueItem> result = catalogueItemService.findAll();

    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(testItem, item2);
    verify(itemRepository).findAllCatalogueItems();
  }

  @Test
  @DisplayName("findByModel() should return item when it exists")
  void testFindByModel() {
    when(itemRepository.findByModel("MODEL-001")).thenReturn(Optional.of(testItem));

    Optional<CatalogueItem> result = catalogueItemService.findByModel("MODEL-001");

    assertThat(result).isPresent();
    assertThat(result.get().model()).isEqualTo("MODEL-001");
    verify(itemRepository).findByModel("MODEL-001");
  }

  @Test
  @DisplayName("findByModel() should return empty when item doesn't exist")
  void testFindByModelNotFound() {
    when(itemRepository.findByModel("NON-EXISTENT")).thenReturn(Optional.empty());

    Optional<CatalogueItem> result = catalogueItemService.findByModel("NON-EXISTENT");

    assertThat(result).isEmpty();
    verify(itemRepository).findByModel("NON-EXISTENT");
  }
}
