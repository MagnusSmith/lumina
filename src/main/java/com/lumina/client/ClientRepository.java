package com.lumina.client;

import com.lumina.client.model.Client;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

interface ClientRepository extends MongoRepository<Client, String> {

  Optional<Client> findById(String id);

}
