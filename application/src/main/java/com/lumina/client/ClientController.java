package com.lumina.client;

import com.lumina.client.dto.ClientDto;
import com.lumina.client.dto.NewClientDto;
import com.lumina.client.dto.UpdateClientDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class ClientController {

  private final ClientService clientService;

  public ClientController(ClientService clientService) {
    this.clientService = clientService;
  }

  @PostMapping("client")
  @ResponseStatus(HttpStatus.CREATED)
  public ClientDto create(@RequestBody NewClientDto newClient) {
    var client = NewClientDto.toModel(newClient);
    return ClientDto.from(clientService.create(client));
  }

  @PutMapping("client")
  public ClientDto update(@RequestBody UpdateClientDto updateClient) {
    var client = UpdateClientDto.toModel(updateClient);
    return ClientDto.from(clientService.update(client));
  }

  @GetMapping("client/{id}")
  public ResponseEntity<ClientDto> getById(@PathVariable String id) {
    return clientService
        .findById(id)
        .map(ClientDto::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @GetMapping("client")
  public List<ClientDto> findAll() {
    return clientService.findAll().stream().map(ClientDto::from).toList();
  }
}
