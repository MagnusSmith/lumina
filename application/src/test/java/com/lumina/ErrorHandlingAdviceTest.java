package com.lumina;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lumina.validation.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
public class ErrorHandlingAdviceTest {

  @InjectMocks private ErrorHandlingAdvice errorHandlingAdvice;

  @Test
  @DisplayName("onConstraintValidationException should return ValidationErrorResponse")
  void testOnConstraintValidationException() {
    // Arrange
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(violation.getPropertyPath()).thenReturn(path);
    when(path.toString()).thenReturn("testField");
    when(violation.getMessage()).thenReturn("must not be null");

    Set<ConstraintViolation<?>> violations = Set.of(violation);
    ConstraintViolationException exception = new ConstraintViolationException(violations);

    // Act
    ValidationErrorResponse response =
        errorHandlingAdvice.onConstraintValidationException(exception);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.violations()).hasSize(1);
    assertThat(response.violations().getFirst().fieldName()).isEqualTo("testField");
    assertThat(response.violations().getFirst().message()).isEqualTo("must not be null");
  }

  @Test
  @DisplayName("onMethodArgumentNotValidException should return ValidationErrorResponse")
  void testOnMethodArgumentNotValidException() {
    // Arrange
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "testObject");
    bindingResult.addError(new FieldError("testObject", "field1", "Error message 1"));
    bindingResult.addError(new FieldError("testObject", "field2", "Error message 2"));

    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    when(exception.getBindingResult()).thenReturn(bindingResult);

    // Act
    ValidationErrorResponse response =
        errorHandlingAdvice.onMethodArgumentNotValidException(exception);

    // Assert
    assertThat(response).isNotNull();
    assertThat(response.violations()).hasSize(2);
    assertThat(response.violations())
        .extracting("fieldName", "message")
        .containsExactlyInAnyOrder(
            tuple("field1", "Error message 1"), tuple("field2", "Error message 2"));
  }

  @Test
  @DisplayName("onNotFoundException should return ValidationErrorResponse")
  void testOnNotFoundException() {
    NotFoundException exception = new NotFoundException("Resource not found");

    ValidationErrorResponse response = errorHandlingAdvice.onNotFoundException(exception);

    assertThat(response).isNotNull();
    assertThat(response.violations()).hasSize(1);
    assertThat(response.violations().getFirst().fieldName()).isEqualTo("resource");
    assertThat(response.violations().getFirst().message()).isEqualTo("Resource not found");
  }

  @Test
  @DisplayName("onDuplicateResourceException should return ValidationErrorResponse")
  void testOnDuplicateResourceException() {
    DuplicateResourceException exception =
        new DuplicateResourceException("Resource already exists");

    ValidationErrorResponse response = errorHandlingAdvice.onDuplicateResourceException(exception);

    assertThat(response).isNotNull();
    assertThat(response.violations()).hasSize(1);
    assertThat(response.violations().getFirst().fieldName()).isEqualTo("resource");
    assertThat(response.violations().getFirst().message()).isEqualTo("Resource already exists");
  }

  @Test
  @DisplayName("onLuminaValidationException should return ValidationErrorResponse")
  void testOnLuminaValidationException() {
    Errors errors = new Errors("testObject");
    errors.rejectValue("field1", ErrorCode.REQUIRED);
    errors.rejectValue("field2", ErrorCode.NOT_FOUND);

    LuminaValidationException exception = new LuminaValidationException(errors);

    ValidationErrorResponse response = errorHandlingAdvice.onLuminaValidationException(exception);

    assertThat(response).isNotNull();
    assertThat(response.violations()).hasSize(2);
  }
}
