package com.lumina.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lumina.NotFoundException;
import com.lumina.client.model.Client;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

  @Mock private ClientRepository repository;

  @InjectMocks private ClientService clientService;

  private Client testClient;

  @BeforeEach
  void setup() {
    testClient = new Client("client-1", "Test Client", List.of(), null, null, null, null);
  }

  @Test
  @DisplayName("create() should save and return a new client")
  void testCreate() {
    when(repository.save(any(Client.class))).thenReturn(testClient);

    Client result = clientService.create(testClient);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("client-1");
    assertThat(result.name()).isEqualTo("Test Client");
    verify(repository).save(testClient);
  }

  @Test
  @DisplayName("update() should update and return existing client")
  void testUpdate() {
    when(repository.existsById("client-1")).thenReturn(true);
    when(repository.save(any(Client.class))).thenReturn(testClient);

    Client result = clientService.update(testClient);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("client-1");
    verify(repository).existsById("client-1");
    verify(repository).save(testClient);
  }

  @Test
  @DisplayName("update() should throw NotFoundException when client doesn't exist")
  void testUpdateNotFound() {
    when(repository.existsById("non-existent")).thenReturn(false);

    Client nonExistentClient = new Client("non-existent", "Non Existent", List.of(), null, null, null, null);

    assertThatThrownBy(() -> clientService.update(nonExistentClient))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("The client with id non-existent could not be found!");

    verify(repository).existsById("non-existent");
  }

  @Test
  @DisplayName("findById() should return client when it exists")
  void testFindById() {
    when(repository.findById("client-1")).thenReturn(Optional.of(testClient));

    Optional<Client> result = clientService.findById("client-1");

    assertThat(result).isPresent();
    assertThat(result.get().id()).isEqualTo("client-1");
    verify(repository).findById("client-1");
  }

  @Test
  @DisplayName("findById() should return empty when client doesn't exist")
  void testFindByIdNotFound() {
    when(repository.findById("non-existent")).thenReturn(Optional.empty());

    Optional<Client> result = clientService.findById("non-existent");

    assertThat(result).isEmpty();
    verify(repository).findById("non-existent");
  }

  @Test
  @DisplayName("findAll() should return all clients")
  void testFindAll() {
    Client client2 = new Client("client-2", "Test Client 2", List.of(), null, null, null, null);
    when(repository.findAll()).thenReturn(List.of(testClient, client2));

    List<Client> result = clientService.findAll();

    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(testClient, client2);
    verify(repository).findAll();
  }

  @Test
  @DisplayName("findAll() should return empty list when no clients exist")
  void testFindAllEmpty() {
    when(repository.findAll()).thenReturn(List.of());

    List<Client> result = clientService.findAll();

    assertThat(result).isEmpty();
    verify(repository).findAll();
  }
}
