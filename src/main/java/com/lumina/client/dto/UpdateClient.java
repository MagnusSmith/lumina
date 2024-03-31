package com.lumina.client.dto;

import com.lumina.client.Client;
import com.lumina.client.ClientBuilder;

public record UpdateClient(
    String id,
    String name
) {
  public Client toClient(){
    return ClientBuilder.builder().id(id).name(name).build();
  }
}
