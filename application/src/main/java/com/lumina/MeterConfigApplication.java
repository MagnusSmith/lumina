package com.lumina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeterConfigApplication {

  public static void main(String[] args) {
    SpringApplication.run(MeterConfigApplication.class, args);
  }
}
