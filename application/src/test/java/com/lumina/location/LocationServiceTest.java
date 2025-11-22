package com.lumina.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lumina.NotFoundException;
import com.lumina.location.model.Location;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

  @Mock private LocationRepository repository;

  @InjectMocks private LocationService locationService;

  private Location testLocation;

  @BeforeEach
  void setup() {
    testLocation = new Location("location-1", "Test Location", "project-1", null);
  }

  @Test
  @DisplayName("create() should save and return a new location")
  void testCreate() {
    when(repository.save(any(Location.class))).thenReturn(testLocation);

    Location result = locationService.create(testLocation);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("location-1");
    assertThat(result.name()).isEqualTo("Test Location");
    assertThat(result.projectId()).isEqualTo("project-1");
    verify(repository).save(testLocation);
  }

  @Test
  @DisplayName("update() should update and return existing location")
  void testUpdate() {
    when(repository.findById("location-1")).thenReturn(Optional.of(testLocation));
    when(repository.save(any(Location.class))).thenReturn(testLocation);

    Location result = locationService.update(testLocation);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("location-1");
    verify(repository).findById("location-1");
    verify(repository).save(testLocation);
  }

  @Test
  @DisplayName("update() should throw NotFoundException when location doesn't exist")
  void testUpdateNotFound() {
    when(repository.findById("non-existent")).thenReturn(Optional.empty());

    Location nonExistentLocation = new Location("non-existent", "Non Existent", "project-1", null);

    assertThatThrownBy(() -> locationService.update(nonExistentLocation))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("The location with id non-existent could not be found!");

    verify(repository).findById("non-existent");
  }

  @Test
  @DisplayName("findById() should return location when it exists")
  void testFindById() {
    when(repository.findById("location-1")).thenReturn(Optional.of(testLocation));

    Optional<Location> result = locationService.findById("location-1");

    assertThat(result).isPresent();
    assertThat(result.get().id()).isEqualTo("location-1");
    verify(repository).findById("location-1");
  }

  @Test
  @DisplayName("findById() should return empty when location doesn't exist")
  void testFindByIdNotFound() {
    when(repository.findById("non-existent")).thenReturn(Optional.empty());

    Optional<Location> result = locationService.findById("non-existent");

    assertThat(result).isEmpty();
    verify(repository).findById("non-existent");
  }
}
