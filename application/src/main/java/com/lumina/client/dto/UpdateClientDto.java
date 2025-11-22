package com.lumina.client.dto;

import com.lumina.client.model.Client;
import com.lumina.client.model.ClientBuilder;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;

public record UpdateClientDto(
    @NotBlank(message = "ID is required") String id,
    @NotBlank(message = "Name is required") String name) {
  public static Client toModel(UpdateClientDto updateClient) {
    return ClientBuilder.builder()
        .id(updateClient.id())
        .name(updateClient.name())
        .projects(new ArrayList<>())
        .build();
  }
}
