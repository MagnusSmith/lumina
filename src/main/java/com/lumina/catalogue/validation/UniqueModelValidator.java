package com.lumina.catalogue.validation;

import com.lumina.catalogue.ItemRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueModelValidator implements ConstraintValidator<UniqueModel, String> {

  private final ItemRepository repos;

  UniqueModelValidator(final ItemRepository repos){
    this.repos = repos;
  }

  @Override
  public boolean isValid(String model, ConstraintValidatorContext constraintValidatorContext) {
    return !repos.existsByModel(model);
  }
}
