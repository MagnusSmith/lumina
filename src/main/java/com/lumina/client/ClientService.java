package com.lumina.client;

import com.lumina.client.model.Client;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    return repository.save(client);
  }

  public Optional<Client> findById(String id) {
    return repository.findById(id);
  }


  public List<Client> findAll() {
    return repository.findAll();
  }
}