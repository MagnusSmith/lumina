package com.lumina.catalogue.model;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ValidationStage {
  Intake,
  Connection,
  Staging,
  Deployment;

  public static final String PATTERN =  Arrays.stream(ValidationStage.values()).map(Enum::name).collect(Collectors.joining("|"));


  public boolean shouldValidateAt(ValidationStage stage){
    return this.compareTo(stage) <= 0;
  }



}
