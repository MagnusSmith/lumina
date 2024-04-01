package com.lumina.client;


import com.lumina.client.model.Client;
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
  public Client create(@RequestBody Client newClient) {
    return clientService.create(newClient);
  }

  @PutMapping("client")
  public Client update(@RequestBody Client updateClient) {
    return clientService.update(updateClient);
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
