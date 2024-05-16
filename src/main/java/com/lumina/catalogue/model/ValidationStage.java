package com.lumina.catalogue.model;

public enum ValidationStage {
  New,
  One,
  Two,
  Three;



  public boolean shouldValidateAt(ValidationStage stage){
    return this.compareTo(stage) >= 0;
  }

}
