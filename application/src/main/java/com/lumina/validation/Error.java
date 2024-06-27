package com.lumina.validation;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record Error(
    String field,
    Object rejectedValue,
    ErrorCode errorCode,
    Object[] errorCodeArgs,
    String fieldContext)
    implements com.lumina.validation.ErrorBuilder.With {

  String propertyPath() {
    if (fieldContext.isEmpty()) {
      return field;
    } else {
      return STR."\{fieldContext}.\{field}";
    }
  }
}
