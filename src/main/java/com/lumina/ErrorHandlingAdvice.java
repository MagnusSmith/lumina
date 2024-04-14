package com.lumina;

import com.lumina.validation.LuminaValidationException;
import com.lumina.validation.ValidationErrorResponse;
import com.lumina.validation.Violation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

@ControllerAdvice
public class ErrorHandlingAdvice {
  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  ValidationErrorResponse onConstraintValidationException(ConstraintViolationException e) {
    var vList = e.getConstraintViolations().stream().map(v -> new Violation(v.getPropertyPath().toString(), v.getMessage()))
        .collect(Collectors.toList());
    return  new ValidationErrorResponse(vList);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  ValidationErrorResponse onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
     var vList = e.getBindingResult().getFieldErrors().stream().map(fe -> new Violation(fe.getField(), fe.getDefaultMessage()))
        .collect(Collectors.toList());
    return  new ValidationErrorResponse(vList);

  }

  @ExceptionHandler(LuminaValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  ValidationErrorResponse onLuminaValidationException(LuminaValidationException e) {
    var vList = e.validationErrors().stream().map(fe -> new Violation(fe.field(), fe.errorCode().defaultDescription()))
        .collect(Collectors.toList());
    return  new ValidationErrorResponse(vList);
  }
}
