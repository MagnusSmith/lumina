package com.lumina.meter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.lumina.NotFoundException;
import com.lumina.catalogue.ItemRepository;
import com.lumina.catalogue.model.CatalogueItem;
import com.lumina.catalogue.model.Level;
import com.lumina.catalogue.model.MeterType;
import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Meter;
import com.lumina.meter.validation.MeterValidator;
import com.lumina.validation.Errors;
import com.lumina.validation.LuminaValidationException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MeterServiceTest {

  @Mock private MeterRepository meterRepository;

  @Mock private ItemRepository catalogueItemRepository;

  @Mock private MeterValidator meterValidator;

  @InjectMocks private MeterService meterService;

  private Meter testMeter;
  private CatalogueItem testCatalogueItem;

  @BeforeEach
  void setup() {
    testMeter =
        new Meter("meter-1", "location-1", "MODEL-001", List.of(), ValidationStage.Connection);

    testCatalogueItem =
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
  @DisplayName("create() should save and return a new meter when validation passes")
  void testCreate() {
    doNothing().when(meterValidator).validate(any(Meter.class), any(Errors.class));
    when(meterRepository.save(any(Meter.class))).thenReturn(testMeter);

    Meter result = meterService.create(testMeter);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("meter-1");
    assertThat(result.model()).isEqualTo("MODEL-001");
    verify(meterValidator).validate(any(Meter.class), any(Errors.class));
    verify(meterRepository).save(testMeter);
  }

  @Test
  @DisplayName("create() should throw LuminaValidationException when validation fails")
  void testCreateValidationFails() {
    doAnswer(
            invocation -> {
              Errors errors = invocation.getArgument(1);
              errors.rejectValue("testField", com.lumina.validation.ErrorCode.REQUIRED);
              return null;
            })
        .when(meterValidator)
        .validate(any(Meter.class), any(Errors.class));

    assertThatThrownBy(() -> meterService.create(testMeter))
        .isInstanceOf(LuminaValidationException.class);

    verify(meterValidator).validate(any(Meter.class), any(Errors.class));
    verify(meterRepository, never()).save(any(Meter.class));
  }

  @Test
  @DisplayName("update() should update and return existing meter when validation passes")
  void testUpdate() {
    when(meterRepository.findById("meter-1")).thenReturn(Optional.of(testMeter));
    doNothing().when(meterValidator).validate(any(Meter.class), any(Errors.class));
    when(meterRepository.save(any(Meter.class))).thenReturn(testMeter);

    Meter result = meterService.update(testMeter);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("meter-1");
    verify(meterValidator).validate(any(Meter.class), any(Errors.class));
    verify(meterRepository).findById("meter-1");
    verify(meterRepository).save(testMeter);
  }

  @Test
  @DisplayName("update() should throw NotFoundException when meter doesn't exist")
  void testUpdateNotFound() {
    when(meterRepository.findById("non-existent")).thenReturn(Optional.empty());
    doNothing().when(meterValidator).validate(any(Meter.class), any(Errors.class));

    Meter nonExistentMeter =
        new Meter("non-existent", "location-1", "MODEL-001", List.of(), ValidationStage.Connection);

    assertThatThrownBy(() -> meterService.update(nonExistentMeter))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("The meter with id non-existent could not be found!");

    verify(meterRepository).findById("non-existent");
  }

  @Test
  @DisplayName("findCatalogueItemByModel() should return catalogue item when it exists")
  void testFindCatalogueItemByModel() {
    when(catalogueItemRepository.findByModel("MODEL-001"))
        .thenReturn(Optional.of(testCatalogueItem));

    CatalogueItem result = meterService.findCatalogueItemByModel("MODEL-001");

    assertThat(result).isNotNull();
    assertThat(result.model()).isEqualTo("MODEL-001");
    verify(catalogueItemRepository).findByModel("MODEL-001");
  }

  @Test
  @DisplayName(
      "findCatalogueItemByModel() should throw NotFoundException when catalogue item doesn't exist")
  void testFindCatalogueItemByModelNotFound() {
    when(catalogueItemRepository.findByModel("NON-EXISTENT")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> meterService.findCatalogueItemByModel("NON-EXISTENT"))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("The meter model NON-EXISTENT could not be found in the catalogue!");

    verify(catalogueItemRepository).findByModel("NON-EXISTENT");
  }

  @Test
  @DisplayName("findById() should return meter when it exists")
  void testFindById() {
    when(meterRepository.findById("meter-1")).thenReturn(Optional.of(testMeter));

    Optional<Meter> result = meterService.findById("meter-1");

    assertThat(result).isPresent();
    assertThat(result.get().id()).isEqualTo("meter-1");
    verify(meterRepository).findById("meter-1");
  }

  @Test
  @DisplayName("findByLocationId() should return all meters for a location")
  void testFindByLocationId() {
    Meter meter2 =
        new Meter("meter-2", "location-1", "MODEL-002", List.of(), ValidationStage.Connection);

    when(meterRepository.findByLocationId("location-1")).thenReturn(List.of(testMeter, meter2));

    List<Meter> result = meterService.findByLocationId("location-1");

    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(testMeter, meter2);
    verify(meterRepository).findByLocationId("location-1");
  }
}
