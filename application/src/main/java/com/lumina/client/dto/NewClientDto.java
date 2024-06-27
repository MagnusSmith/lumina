package com.lumina.client.dto;

import com.lumina.client.model.Client;
import com.lumina.client.model.ClientBuilder;
import java.util.ArrayList;

public record NewClientDto(String name) {

    public static Client toModel(NewClientDto newClient) {
        return ClientBuilder.builder().name(newClient.name()).projects(new ArrayList<>()).build();
    }
}
