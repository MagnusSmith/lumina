package com.lumina.catalogue;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.lumina.catalogue.model.CatalogueItem;
import com.lumina.catalogue.model.Level;
import com.lumina.catalogue.model.MeterType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CatalogueController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CatalogueControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CatalogueItemService itemService;

  @MockitoBean private CataloguePresetService presetService;

  @Test
  @DisplayName("GET /api/catalogue/models should return all model summaries")
  void testGetModels() throws Exception {
    CatalogueItem item1 =
        new CatalogueItem(
            "item-1",
            "GATEWAY-LORAWAN-V1",
            Level.GATEWAY,
            MeterType.LORAWAN,
            "LoRaWAN Gateway",
            "Manufacturer A",
            List.of(),
            List.of());
    CatalogueItem item2 =
        new CatalogueItem(
            "item-2",
            "DEVICE-MODBUS-V1",
            Level.DEVICE,
            MeterType.MODBUS,
            "Modbus Device",
            "Manufacturer B",
            List.of(),
            List.of());

    when(itemService.findAll()).thenReturn(List.of(item1, item2));

    mockMvc
        .perform(get("/api/catalogue/models"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].model").value("GATEWAY-LORAWAN-V1"))
        .andExpect(jsonPath("$[0].description").value("LoRaWAN Gateway"))
        .andExpect(jsonPath("$[1].model").value("DEVICE-MODBUS-V1"))
        .andExpect(jsonPath("$[1].description").value("Modbus Device"));
  }

  @Test
  @DisplayName("GET /api/catalogue/models should return empty array when no items")
  void testGetModelsEmpty() throws Exception {
    when(itemService.findAll()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/catalogue/models"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName("GET /api/catalogue/items should return all catalogue items")
  void testGetItems() throws Exception {
    CatalogueItem item1 =
        new CatalogueItem(
            "item-1",
            "GATEWAY-LORAWAN-V1",
            Level.GATEWAY,
            MeterType.LORAWAN,
            "LoRaWAN Gateway",
            "Manufacturer A",
            List.of(),
            List.of());

    when(itemService.findAll()).thenReturn(List.of(item1));

    mockMvc
        .perform(get("/api/catalogue/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].model").value("GATEWAY-LORAWAN-V1"))
        .andExpect(jsonPath("$[0].level").value("GATEWAY"))
        .andExpect(jsonPath("$[0].type").value("LORAWAN"));
  }

  @Test
  @DisplayName("GET /api/catalogue/item/{model} should return item when it exists")
  void testGetItemByModel() throws Exception {
    CatalogueItem item =
        new CatalogueItem(
            "item-1",
            "GATEWAY-LORAWAN-V1",
            Level.GATEWAY,
            MeterType.LORAWAN,
            "LoRaWAN Gateway",
            "Manufacturer A",
            List.of(),
            List.of());

    when(itemService.findByModel("GATEWAY-LORAWAN-V1")).thenReturn(Optional.of(item));

    mockMvc
        .perform(get("/api/catalogue/item/GATEWAY-LORAWAN-V1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.model").value("GATEWAY-LORAWAN-V1"))
        .andExpect(jsonPath("$.level").value("GATEWAY"))
        .andExpect(jsonPath("$.type").value("LORAWAN"))
        .andExpect(jsonPath("$.description").value("LoRaWAN Gateway"));
  }

  @Test
  @DisplayName("GET /api/catalogue/item/{model} should return 404 when item doesn't exist")
  void testGetItemByModelNotFound() throws Exception {
    when(itemService.findByModel("NON-EXISTENT")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/catalogue/item/NON-EXISTENT")).andExpect(status().isNotFound());
  }
}
