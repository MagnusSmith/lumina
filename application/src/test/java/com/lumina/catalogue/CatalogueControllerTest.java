package com.lumina.catalogue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.catalogue.dto.NewCatalogueItemDto;
import com.lumina.catalogue.dto.PresetDto;
import com.lumina.catalogue.dto.UpdateCatalogueItemDto;
import com.lumina.catalogue.model.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CatalogueController.class)
public class CatalogueControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CatalogueService.Item itemService;

  @MockitoBean private CatalogueService.PresetService presetService;

  @Test
  @DisplayName("POST /api/catalogue/item should create a new catalogue item and return 201")
  void testCreateCatalogueItem() throws Exception {
    NewCatalogueItemDto newItemDto =
        new NewCatalogueItemDto(
            "MODEL-001", Level.DEVICE, MeterType.LORAWAN, "Test Item", "Manufacturer A");
    CatalogueItem savedItem =
        new CatalogueItem(
            "item-1",
            "MODEL-001",
            Level.DEVICE,
            MeterType.LORAWAN,
            "Test Item",
            "Manufacturer A",
            List.of(),
            List.of());

    when(presetService.findByTypeAndLevel(MeterType.LORAWAN, Level.DEVICE))
        .thenReturn(Optional.empty());
    when(itemService.create(any(CatalogueItem.class))).thenReturn(savedItem);

    mockMvc
        .perform(
            post("/api/catalogue/item")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newItemDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("item-1"))
        .andExpect(jsonPath("$.model").value("MODEL-001"))
        .andExpect(jsonPath("$.level").value("DEVICE"))
        .andExpect(jsonPath("$.type").value("LORAWAN"));
  }

  @Test
  @DisplayName("PUT /api/catalogue/item should update existing catalogue item and return 200")
  void testUpdateCatalogueItem() throws Exception {
    UpdateCatalogueItemDto updateItemDto =
        new UpdateCatalogueItemDto(
            "item-1",
            "MODEL-001",
            Level.DEVICE,
            MeterType.LORAWAN,
            "Updated Item",
            "Manufacturer A",
            List.of(),
            List.of());
    CatalogueItem updatedItem =
        new CatalogueItem(
            "item-1",
            "MODEL-001",
            Level.DEVICE,
            MeterType.LORAWAN,
            "Updated Item",
            "Manufacturer A",
            List.of(),
            List.of());

    when(itemService.update(any(CatalogueItem.class))).thenReturn(updatedItem);

    mockMvc
        .perform(
            put("/api/catalogue/item")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateItemDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("item-1"))
        .andExpect(jsonPath("$.description").value("Updated Item"));
  }

  @Test
  @DisplayName("DELETE /api/catalogue/item/{model} should delete catalogue item")
  void testDeleteCatalogueItem() throws Exception {
    mockMvc.perform(delete("/api/catalogue/item/MODEL-001")).andExpect(status().isOk());

    verify(itemService).delete("MODEL-001");
  }

  @Test
  @DisplayName("GET /api/catalogue/items should return all catalogue items")
  void testGetAllCatalogueItems() throws Exception {
    CatalogueItem item1 =
        new CatalogueItem(
            "item-1",
            "MODEL-001",
            Level.DEVICE,
            MeterType.LORAWAN,
            "Item 1",
            "Manufacturer A",
            List.of(),
            List.of());
    CatalogueItem item2 =
        new CatalogueItem(
            "item-2",
            "MODEL-002",
            Level.GATEWAY,
            MeterType.MOBIUS,
            "Item 2",
            "Manufacturer B",
            List.of(),
            List.of());

    when(itemService.findAll()).thenReturn(List.of(item1, item2));

    mockMvc
        .perform(get("/api/catalogue/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].model").value("MODEL-001"))
        .andExpect(jsonPath("$[1].model").value("MODEL-002"));
  }

  @Test
  @DisplayName("GET /api/catalogue/item/{model} should return catalogue item when it exists")
  void testGetCatalogueItemByModel() throws Exception {
    CatalogueItem item =
        new CatalogueItem(
            "item-1",
            "MODEL-001",
            Level.DEVICE,
            MeterType.LORAWAN,
            "Test Item",
            "Manufacturer A",
            List.of(),
            List.of());

    when(itemService.findByModel("MODEL-001")).thenReturn(Optional.of(item));

    mockMvc
        .perform(get("/api/catalogue/item/MODEL-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.model").value("MODEL-001"))
        .andExpect(jsonPath("$.description").value("Test Item"));
  }

  @Test
  @DisplayName("GET /api/catalogue/item/{model} should return 404 when item doesn't exist")
  void testGetCatalogueItemByModelNotFound() throws Exception {
    when(itemService.findByModel("NON-EXISTENT")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/catalogue/item/NON-EXISTENT")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/catalogue/preset should create a new preset and return 201")
  void testCreatePreset() throws Exception {
    PresetDto.New newPresetDto =
        new PresetDto.New(MeterType.LORAWAN, Level.DEVICE, List.of(), List.of());
    Preset savedPreset = new Preset("preset-1", Level.DEVICE, MeterType.LORAWAN, List.of(), List.of());

    when(presetService.create(any(Preset.class))).thenReturn(savedPreset);

    mockMvc
        .perform(
            post("/api/catalogue/preset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPresetDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("preset-1"))
        .andExpect(jsonPath("$.type").value("LORAWAN"))
        .andExpect(jsonPath("$.level").value("DEVICE"));
  }

  @Test
  @DisplayName("PUT /api/catalogue/preset should update existing preset and return 200")
  void testUpdatePreset() throws Exception {
    PresetDto.Update updatePresetDto =
        new PresetDto.Update("preset-1", MeterType.LORAWAN, Level.DEVICE, List.of(), List.of());
    Preset updatedPreset =
        new Preset("preset-1", Level.DEVICE, MeterType.LORAWAN, List.of(), List.of());

    when(presetService.update(any(Preset.class))).thenReturn(updatedPreset);

    mockMvc
        .perform(
            put("/api/catalogue/preset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePresetDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("preset-1"));
  }

  @Test
  @DisplayName("GET /api/catalogue/preset/{type}/{level} should return preset when it exists")
  void testGetPresetByTypeAndLevel() throws Exception {
    Preset preset = new Preset("preset-1", Level.DEVICE, MeterType.LORAWAN, List.of(), List.of());

    when(presetService.findByTypeAndLevel(MeterType.LORAWAN, Level.DEVICE))
        .thenReturn(Optional.of(preset));

    mockMvc
        .perform(get("/api/catalogue/preset/LORAWAN/DEVICE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("preset-1"))
        .andExpect(jsonPath("$.type").value("LORAWAN"))
        .andExpect(jsonPath("$.level").value("DEVICE"));
  }

  @Test
  @DisplayName("GET /api/catalogue/preset/{type}/{level} should return 404 when preset doesn't exist")
  void testGetPresetByTypeAndLevelNotFound() throws Exception {
    when(presetService.findByTypeAndLevel(MeterType.MOBIUS, Level.GATEWAY))
        .thenReturn(Optional.empty());

    mockMvc.perform(get("/api/catalogue/preset/MOBIUS/GATEWAY")).andExpect(status().isNotFound());
  }
}
