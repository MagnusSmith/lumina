package com.lumina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.mongodb.MongoManagedTypes;

@SpringBootApplication

public class MeterConfigApplication {

  public static void main(String[] args) {
    SpringApplication.run(MeterConfigApplication.class, args);
  }


}
