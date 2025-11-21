package com.lumina.client;

import com.lumina.client.dto.ClientDto;
import com.lumina.client.dto.NewClientDto;
import com.lumina.client.dto.UpdateClientDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
@Tag(name = "Client", description = "Client management APIs")
public class ClientController {

  private final ClientService clientService;

  public ClientController(ClientService clientService) {
    this.clientService = clientService;
  }

  @Operation(summary = "Create a new client", description = "Creates a new client in the system")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Client created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
      })
  @PostMapping("client")
  @ResponseStatus(HttpStatus.CREATED)
  public ClientDto create(@RequestBody @Valid NewClientDto newClient) {
    var client = NewClientDto.toModel(newClient);
    return ClientDto.from(clientService.create(client));
  }

  @Operation(summary = "Update an existing client", description = "Updates client information")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Client updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Client not found")
      })
  @PutMapping("client")
  public ClientDto update(@RequestBody @Valid UpdateClientDto updateClient) {
    var client = UpdateClientDto.toModel(updateClient);
    return ClientDto.from(clientService.update(client));
  }

  @Operation(summary = "Get client by ID", description = "Retrieves a client by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Client found"),
        @ApiResponse(responseCode = "404", description = "Client not found")
      })
  @GetMapping("client/{id}")
  public ResponseEntity<ClientDto> getById(@PathVariable String id) {
    return clientService
        .findById(id)
        .map(ClientDto::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @Operation(summary = "Get all clients", description = "Retrieves all clients in the system")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Clients retrieved")})
  @GetMapping("client")
  public List<ClientDto> findAll() {
    return clientService.findAll().stream().map(ClientDto::from).toList();
  }
}
