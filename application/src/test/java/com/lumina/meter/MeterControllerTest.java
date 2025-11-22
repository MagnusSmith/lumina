package com.lumina.meter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.catalogue.model.*;
import com.lumina.meter.dto.MeterDto;
import com.lumina.meter.dto.NewMeterDto;
import com.lumina.meter.dto.UpdateMeterDto;
import com.lumina.meter.model.Meter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MeterController.class)
public class MeterControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private MeterService meterService;

  private CatalogueItem createTestCatalogueItem() {
    return new CatalogueItem(
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
  @DisplayName("POST /api/meter should create a new meter and return 201")
  void testCreateMeter() throws Exception {
    NewMeterDto newMeterDto =
        new NewMeterDto("location-1", "MODEL-001", List.of(), ValidationStage.Connection);
    Meter savedMeter =
        new Meter("meter-1", "location-1", "MODEL-001", List.of(), ValidationStage.Connection);
    CatalogueItem catalogueItem = createTestCatalogueItem();

    when(meterService.findCatalogueItemByModel("MODEL-001")).thenReturn(catalogueItem);
    when(meterService.create(any(Meter.class))).thenReturn(savedMeter);

    mockMvc
        .perform(
            post("/api/meter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMeterDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("meter-1"))
        .andExpect(jsonPath("$.locationId").value("location-1"))
        .andExpect(jsonPath("$.model").value("MODEL-001"));
  }

  @Test
  @DisplayName("PUT /api/meter should update existing meter and return 200")
  void testUpdateMeter() throws Exception {
    UpdateMeterDto updateMeterDto =
        new UpdateMeterDto(
            "meter-1", "location-1", "MODEL-001", List.of(), ValidationStage.Connection);
    Meter updatedMeter =
        new Meter("meter-1", "location-1", "MODEL-001", List.of(), ValidationStage.Connection);
    CatalogueItem catalogueItem = createTestCatalogueItem();

    when(meterService.update(any(Meter.class))).thenReturn(updatedMeter);
    when(meterService.findCatalogueItemByModel("MODEL-001")).thenReturn(catalogueItem);

    mockMvc
        .perform(
            put("/api/meter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateMeterDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("meter-1"))
        .andExpect(jsonPath("$.model").value("MODEL-001"));
  }

  @Test
  @DisplayName("GET /api/meter/{id} should return meter when it exists")
  void testGetMeterById() throws Exception {
    Meter meter =
        new Meter("meter-1", "location-1", "MODEL-001", List.of(), ValidationStage.Connection);
    MeterDto meterDto =
        new MeterDto("meter-1", "location-1", "MODEL-001", List.of(), ValidationStage.Connection);

    when(meterService.findById("meter-1")).thenReturn(Optional.of(meter));
    when(meterService.toMeterDto(meter, false)).thenReturn(meterDto);

    mockMvc
        .perform(get("/api/meter/meter-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("meter-1"))
        .andExpect(jsonPath("$.locationId").value("location-1"));
  }

  @Test
  @DisplayName("GET /api/meter/{id} should return 404 when meter doesn't exist")
  void testGetMeterByIdNotFound() throws Exception {
    when(meterService.findById("non-existent")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/meter/non-existent")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/meter/{id} with withConstraints param should include constraints")
  void testGetMeterByIdWithConstraints() throws Exception {
    Meter meter =
        new Meter("meter-1", "location-1", "MODEL-001", List.of(), ValidationStage.Connection);
    MeterDto meterDto =
        new MeterDto("meter-1", "location-1", "MODEL-001", List.of(), ValidationStage.Connection);

    when(meterService.findById("meter-1")).thenReturn(Optional.of(meter));
    when(meterService.toMeterDto(meter, true)).thenReturn(meterDto);

    mockMvc
        .perform(get("/api/meter/meter-1").param("withConstraints", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("meter-1"));
  }

  @Test
  @DisplayName("GET /api/meter/location/{locationId} should return all meters for location")
  void testGetMetersByLocationId() throws Exception {
    Meter meter1 =
        new Meter("meter-1", "location-1", "MODEL-001", List.of(), ValidationStage.Connection);
    Meter meter2 =
        new Meter("meter-2", "location-1", "MODEL-002", List.of(), ValidationStage.Connection);
    MeterDto meterDto1 =
        new MeterDto("meter-1", "location-1", "MODEL-001", List.of(), ValidationStage.Connection);
    MeterDto meterDto2 =
        new MeterDto("meter-2", "location-1", "MODEL-002", List.of(), ValidationStage.Connection);

    when(meterService.findByLocationId("location-1")).thenReturn(List.of(meter1, meter2));
    when(meterService.toMeterDto(meter1, false)).thenReturn(meterDto1);
    when(meterService.toMeterDto(meter2, false)).thenReturn(meterDto2);

    mockMvc
        .perform(get("/api/meter/location/location-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value("meter-1"))
        .andExpect(jsonPath("$[1].id").value("meter-2"));
  }

  @Test
  @DisplayName("GET /api/meter/location/{locationId} should return empty array when no meters exist")
  void testGetMetersByLocationIdEmpty() throws Exception {
    when(meterService.findByLocationId("location-1")).thenReturn(List.of());

    mockMvc
        .perform(get("/api/meter/location/location-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
