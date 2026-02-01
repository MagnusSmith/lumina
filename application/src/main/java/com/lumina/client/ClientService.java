package com.lumina.client;

import com.lumina.NotFoundException;
import com.lumina.client.model.Client;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  /**
   * Updates a client. This method validates that the client exists before performing the update.
   *
   * @param client the client with updated data
   * @return the updated client
   * @throws NotFoundException if the client does not exist
   */
  public Client update(Client client) {
    if (!repository.existsById(client.id())) {
      throw new NotFoundException(
          "The client with id %s could not be found!".formatted(client.id()));
    }
    return repository.save(client);
  }

  public Optional<Client> findById(String id) {
    return repository.findById(id);
  }

  public List<Client> findAllById(Collection<String> ids) {
    return repository.findAllById(ids);
  }

  public List<Client> findAll() {
    return repository.findAll();
  }

  /**
   * Retrieves a paginated list of clients.
   *
   * @param pageable the pagination information (page number, size, sort)
   * @return a page of clients
   */
  public Page<Client> findAll(Pageable pageable) {
    return repository.findAll(pageable);
  }

  /**
   * Deletes a client by ID. Validates that the client has no associated projects.
   *
   * @param id the client ID
   * @throws NotFoundException if the client does not exist
   * @throws IllegalStateException if the client has associated projects
   */
  public void delete(String id) {
    Client client =
        repository
            .findById(id)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "The client with id %s could not be found!".formatted(id)));

    if (client.projects() != null && !client.projects().isEmpty()) {
      throw new IllegalStateException(
          "Cannot delete client with existing projects. Delete all projects first.");
    }

    repository.deleteById(id);
  }
}
