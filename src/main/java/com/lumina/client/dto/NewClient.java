package com.lumina.client.dto;

import com.lumina.client.Client;
import com.lumina.client.ClientBuilder;

public record NewClient(
    String name
) {
  public Client toClient(){
    return ClientBuilder.builder().name(name).build();
  }
}
