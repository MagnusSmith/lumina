package com.lumina.validation;

public enum ErrorCode {
  LESS_THAN("isLessThan", "value is too small"),
  GREATER_THAN("isGreaterThan", "value is too large"),
  NOT_INTEGER("isNotInteger", "value is not an integer"),
  WRONG_TYPE("wrongType", "value is of the wrong type"),
  MIN_LENGTH("minLength", "value is too short"),
  MAX_LENGTH("maxLength", "value is too long"),
  NOT_EMPTY("notEmpty", ""),
  REQUIRED("requiredField", "field is required"),
  NOT_FOUND("notFound", "could not be found");

  private final String code;
  private final String defaultDescription;

  ErrorCode(String code, String defaultDescription) {
    this.code = code;
    this.defaultDescription = defaultDescription;
  }

  public String defaultDescription() {
    return defaultDescription;
  }

  public String code() {
    return code;
  }
}
