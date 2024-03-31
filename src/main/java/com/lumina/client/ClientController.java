package com.lumina.client;

import com.lumina.client.dto.NewClient;
import com.lumina.client.dto.UpdateClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClientController {

  private final ClientService clientService;

  public ClientController(ClientService clientService) {
    this.clientService = clientService;
  }


  @PostMapping("client")
  @ResponseStatus(HttpStatus.CREATED)
  public Client create(@RequestBody NewClient newClient) {
    return clientService.create(newClient.toClient());
  }

  @PutMapping("client")
  public Client update(@RequestBody UpdateClient updateClient) {
    return clientService.update(updateClient.toClient());
  }

  @GetMapping("client/{id}")
  public ResponseEntity<Client> getById(@PathVariable String id){
    return clientService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @GetMapping("client")
  public List<Client> findAll(){
    return clientService.findAll();
  }

}
