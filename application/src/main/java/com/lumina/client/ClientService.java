package com.lumina.client;

import com.lumina.NotFoundException;
import com.lumina.client.model.Client;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

  private final ClientRepository repository;

  public ClientService(ClientRepository repository) {
    this.repository = repository;
  }

  public Client create(Client client) {
    return repository.save(client);
  }

  public Client update(Client client) {
    repository
        .findById(client.id())
        .orElseThrow(
            () ->
                new NotFoundException(
                    "The client with id %s could not be found!".formatted(client.id())));
    return repository.save(client);
  }

  public Optional<Client> findById(String id) {
    return repository.findById(id);
  }

  public List<Client> findAll() {
    return repository.findAll();
  }
}
