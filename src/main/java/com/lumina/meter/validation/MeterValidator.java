package com.lumina.meter.validation;

import com.lumina.catalogue.CatalogueService;
import com.lumina.catalogue.model.Constraint;

import com.lumina.meter.model.Line;
import com.lumina.meter.model.Meter;
import static com.lumina.validation.ErrorCode.*;
import com.lumina.validation.Errors;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
public class MeterValidator {

  private final CatalogueService catalogueService;

  public MeterValidator(CatalogueService catalogueService) {
    this.catalogueService = catalogueService;
  }

  public void validate(Meter meter, Errors errors) {
    Objects.requireNonNull(meter);
    Objects.requireNonNull(errors);

    errors.rejectIfEmpty("locationId", meter.locationId(), NOT_EMPTY)
    .rejectIfEmpty("model", meter.model(), NOT_EMPTY);

    catalogueService.findByModel(meter.model()).ifPresentOrElse(
        model -> checkLines(meter.lines(), model.constraints(), errors),
        () -> errors.rejectValue("model", NOT_FOUND));
  }




  void checkLines(List<Line> lines, List<Constraint<Line>> constraints, Errors errors){
    forEachWithCounter(lines,
        (i, line) -> constraints.stream()
            .filter(c -> line.name().equals(c.name()))
            .findFirst()
            .ifPresent(validateLine(i, line, errors)));
   constraints.stream().filter(Constraint::isRequired).forEach(
        con -> lines.stream()
            .filter(l -> con.name().equals(l.name()))
            .findFirst()
            .ifPresentOrElse(_ -> {}, () -> errorLine(con.name(),  errors))
    );
  }


  Consumer<? super Constraint<Line>> validateLine(int index, Line line, Errors errors){
   return cs -> {
      errors.pushContext("lines[%d]".formatted(index));
      cs.validate(line, errors);
      errors.popContext();;
    };
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


}
