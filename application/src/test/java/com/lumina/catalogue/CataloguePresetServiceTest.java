package com.lumina.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lumina.DuplicateResourceException;
import com.lumina.catalogue.model.Level;
import com.lumina.catalogue.model.MeterType;
import com.lumina.catalogue.model.Preset;
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
public class CataloguePresetServiceTest {

  @Mock private ItemRepository itemRepository;

  @InjectMocks private CataloguePresetService presetService;

  private Preset testPreset;

  @BeforeEach
  void setup() {
    testPreset = new Preset("preset-1", Level.DEVICE, MeterType.LORAWAN, List.of(), List.of());
  }

  @Test
  @DisplayName("create() should save and return a new preset when no duplicate exists")
  void testCreate() {
    when(itemRepository.findByTypeAndLevel(MeterType.LORAWAN, Level.DEVICE))
        .thenReturn(Optional.empty());
    when(itemRepository.insert(any(Preset.class))).thenReturn(testPreset);

    Preset result = presetService.create(testPreset);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("preset-1");
    assertThat(result.type()).isEqualTo(MeterType.LORAWAN);
    assertThat(result.level()).isEqualTo(Level.DEVICE);
    verify(itemRepository).findByTypeAndLevel(MeterType.LORAWAN, Level.DEVICE);
    verify(itemRepository).insert(testPreset);
  }

  @Test
  @DisplayName("create() should throw DuplicateResourceException when preset already exists")
  void testCreateDuplicate() {
    when(itemRepository.findByTypeAndLevel(MeterType.LORAWAN, Level.DEVICE))
        .thenReturn(Optional.of(testPreset));

    assertThatThrownBy(() -> presetService.create(testPreset))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("A preset for type LORAWAN and level DEVICE already exists.");

    verify(itemRepository).findByTypeAndLevel(MeterType.LORAWAN, Level.DEVICE);
  }

  @Test
  @DisplayName("update() should update and return existing preset")
  void testUpdate() {
    when(itemRepository.save(any(Preset.class))).thenReturn(testPreset);

    Preset result = presetService.update(testPreset);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("preset-1");
    verify(itemRepository).save(testPreset);
  }

  @Test
  @DisplayName("findByTypeAndLevel() should return preset when it exists")
  void testFindByTypeAndLevel() {
    when(itemRepository.findByTypeAndLevel(MeterType.LORAWAN, Level.DEVICE))
        .thenReturn(Optional.of(testPreset));

    Optional<Preset> result = presetService.findByTypeAndLevel(MeterType.LORAWAN, Level.DEVICE);

    assertThat(result).isPresent();
    assertThat(result.get().type()).isEqualTo(MeterType.LORAWAN);
    assertThat(result.get().level()).isEqualTo(Level.DEVICE);
    verify(itemRepository).findByTypeAndLevel(MeterType.LORAWAN, Level.DEVICE);
  }

  @Test
  @DisplayName("findByTypeAndLevel() should return empty when preset doesn't exist")
  void testFindByTypeAndLevelNotFound() {
    when(itemRepository.findByTypeAndLevel(MeterType.MODBUS, Level.GATEWAY))
        .thenReturn(Optional.empty());

    Optional<Preset> result = presetService.findByTypeAndLevel(MeterType.MODBUS, Level.GATEWAY);

    assertThat(result).isEmpty();
    verify(itemRepository).findByTypeAndLevel(MeterType.MODBUS, Level.GATEWAY);
  }
}
