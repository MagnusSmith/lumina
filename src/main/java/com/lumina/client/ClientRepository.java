package com.lumina.client;

import com.lumina.client.model.Client;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

interface ClientRepository extends MongoRepository<Client, String> {

  Optional<Client> findById(String id);

}
