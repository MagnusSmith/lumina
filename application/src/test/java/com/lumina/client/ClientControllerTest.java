package com.lumina.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.client.dto.NewClientDto;
import com.lumina.client.dto.UpdateClientDto;
import com.lumina.client.model.Client;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClientController.class)
public class ClientControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ClientService clientService;

  @Test
  @DisplayName("POST /api/client should create a new client and return 201")
  void testCreateClient() throws Exception {
    NewClientDto newClientDto = new NewClientDto("Test Client");
    Client savedClient = new Client("client-1", "Test Client", List.of());

    when(clientService.create(any(Client.class))).thenReturn(savedClient);

    mockMvc
        .perform(
            post("/api/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newClientDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("client-1"))
        .andExpect(jsonPath("$.name").value("Test Client"));
  }

  @Test
  @DisplayName("POST /api/client with empty name should return 400")
  void testCreateClientWithEmptyName() throws Exception {
    NewClientDto newClientDto = new NewClientDto("");

    mockMvc
        .perform(
            post("/api/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newClientDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/client with blank name should return 400")
  void testCreateClientWithBlankName() throws Exception {
    NewClientDto newClientDto = new NewClientDto("   ");

    mockMvc
        .perform(
            post("/api/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newClientDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PUT /api/client should update existing client and return 200")
  void testUpdateClient() throws Exception {
    UpdateClientDto updateClientDto = new UpdateClientDto("client-1", "Updated Client");
    Client updatedClient = new Client("client-1", "Updated Client", List.of());

    when(clientService.update(any(Client.class))).thenReturn(updatedClient);

    mockMvc
        .perform(
            put("/api/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateClientDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("client-1"))
        .andExpect(jsonPath("$.name").value("Updated Client"));
  }

  @Test
  @DisplayName("PUT /api/client with empty name should return 400")
  void testUpdateClientWithEmptyName() throws Exception {
    UpdateClientDto updateClientDto = new UpdateClientDto("client-1", "");

    mockMvc
        .perform(
            put("/api/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateClientDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("PUT /api/client with empty id should return 400")
  void testUpdateClientWithEmptyId() throws Exception {
    UpdateClientDto updateClientDto = new UpdateClientDto("", "Updated Name");

    mockMvc
        .perform(
            put("/api/client")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateClientDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/client/{id} should return client when it exists")
  void testGetClientById() throws Exception {
    Client client = new Client("client-1", "Test Client", List.of());

    when(clientService.findById("client-1")).thenReturn(Optional.of(client));

    mockMvc
        .perform(get("/api/client/client-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("client-1"))
        .andExpect(jsonPath("$.name").value("Test Client"));
  }

  @Test
  @DisplayName("GET /api/client/{id} should return 404 when client doesn't exist")
  void testGetClientByIdNotFound() throws Exception {
    when(clientService.findById("non-existent")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/client/non-existent")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/client should return all clients")
  void testGetAllClients() throws Exception {
    Client client1 = new Client("client-1", "Client 1", List.of());
    Client client2 = new Client("client-2", "Client 2", List.of());

    when(clientService.findAll()).thenReturn(List.of(client1, client2));

    mockMvc
        .perform(get("/api/client"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value("client-1"))
        .andExpect(jsonPath("$[0].name").value("Client 1"))
        .andExpect(jsonPath("$[1].id").value("client-2"))
        .andExpect(jsonPath("$[1].name").value("Client 2"));
  }

  @Test
  @DisplayName("GET /api/client should return empty array when no clients exist")
  void testGetAllClientsEmpty() throws Exception {
    when(clientService.findAll()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/client"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }
}
