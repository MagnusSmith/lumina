package com.lumina.client.dto;

import com.lumina.client.model.Client;
import com.lumina.client.model.ClientBuilder;
import java.util.ArrayList;

public record UpdateClientDto(String id, String name) {
    public static Client toModel(UpdateClientDto updateClient) {
        return ClientBuilder.builder()
                .id(updateClient.id())
                .name(updateClient.name())
                .projects(new ArrayList<>())
                .build();
    }
}
