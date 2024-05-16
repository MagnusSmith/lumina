package com.lumina.meter.validation;

import com.lumina.catalogue.CatalogueService;
import com.lumina.catalogue.model.Constraint;

import com.lumina.catalogue.model.NumberLineConstraint;
import com.lumina.catalogue.model.TextLineConstraint;
import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Line;
import com.lumina.meter.model.Meter;

import static com.lumina.meter.validation.MeterValidator.FunctionalHelper.when;
import static com.lumina.validation.ErrorCode.*;
import com.lumina.validation.Errors;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import static com.lumina.meter.validation.MeterValidator.FunctionalHelper.*;

@Component
public class MeterValidator {

  private final CatalogueService.Item itemService;

  public MeterValidator(CatalogueService.Item itemService) {
    this.itemService = itemService;
  }

  public void validate(Meter meter, Errors errors) {
    Objects.requireNonNull(meter);
    Objects.requireNonNull(errors);

    errors.rejectIfEmpty("locationId", meter.locationId(), NOT_EMPTY)
    .rejectIfEmpty("model", meter.model(), NOT_EMPTY);

    itemService.findByModel(meter.model()).ifPresentOrElse(
        model -> checkLines(meter, model.constraints(), errors),
        () -> errors.rejectValue("model", NOT_FOUND));
  }




  void checkLines(Meter meter, List<Constraint<? extends Line>> constraints, Errors errors){
    forEachWithCounter(meter.lines(),
        (i, line) -> constraints.stream()
            .filter(c -> line.name().equals(c.name()))
            .findFirst()
            .ifPresent(cs -> validateLine(cs, line, i , errors, meter.stage())));
   constraints.stream().filter(c -> meter.stage().shouldValidateAt(c.stage())).filter(Constraint::isRequired).forEach(
        con -> meter.lines().stream()
            .filter(l -> con.name().equals(l.name()))
            .findFirst()
            .ifPresentOrElse(_ -> {}, () -> errorLine(con.name(),  errors))
    );
  }

 void validateLine(Constraint<? extends Line> constraint, Line line, int index, Errors errors, ValidationStage stage){
   errors.pushContext("lines[%d]".formatted(index));
    switch(when(constraint, line)){
      case When(NumberLineConstraint nlc, Line.Number nl) ->  nlc.validate(nl, errors, stage);
      case When(TextLineConstraint tlc, Line.Text lt) -> tlc.validate(lt, errors, stage);
      default -> throw new IllegalStateException("Unexpected value: " + when(constraint, line));
    }
   errors.popContext();


  }


  void errorLine(String field, Errors errors){
    errors.pushContext("lines");
    errors.rejectValue(field, REQUIRED);
    errors.popContext();
  }


  static <T> void forEachWithCounter(Iterable<T> source, BiConsumer<Integer, T> consumer) {
    int i = 0;
    for (T item : source) {
      consumer.accept(i, item);
      i++;
    }
  }


  public static class FunctionalHelper {
    public static When when(Constraint<? extends Line> constraint, Line line) {
      return new When(constraint, line);
    }

  public record  When(Constraint<? extends Line> constraint, Line line){}
  }
}
