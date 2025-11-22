package com.lumina.meter.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lumina.catalogue.CatalogueItemService;
import com.lumina.catalogue.model.*;
import com.lumina.catalogue.model.CatalogueItemBuilder;
import com.lumina.catalogue.model.constraint.Constraint;
import com.lumina.catalogue.model.constraint.NumberLineConstraintBuilder;
import com.lumina.catalogue.model.constraint.TextLineConstraintBuilder;
import com.lumina.meter.model.*;
import com.lumina.meter.model.MeterBuilder;
import com.lumina.validation.Errors;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MeterValidatorTest {

  CatalogueItem item;

  CatalogueItemService catalogueService = mock(CatalogueItemService.class);

  @Test
  @DisplayName("A Meter with optional lines should be valid when all lines are valid")
  void shouldValidateMeterWithOptionalWithoutErrors() {

    item = setUpCatalogueItem();
    when(catalogueService.findByModel("A0001")).thenReturn(Optional.of(item));

    Line l1 = new Line.Number("lineOne", NumberType.INTEGER, 3d);
    Line l2 = new Line.Number("lineTwo", NumberType.FLOAT, 9.999d);
    Line l3 = new Line.Text("lineThree", "Hello World!");
    Line l4 = new Line.Text("lineFour", "ABC");

    Meter meter =
        MeterBuilder.builder()
            .locationId("location1")
            .model("A0001")
            .lines(List.of(l1, l2, l3, l4))
            .stage(ValidationStage.Connection)
            .build();
    MeterValidator validator = new MeterValidator(catalogueService);
    Errors errors = new Errors("meter");
    validator.validate(meter, errors);

    assertThat(errors.getErrorCount()).isZero();
  }

  @Test
  @DisplayName("A Meter without optional lines should be valid when all lines are valid")
  void shouldValidateMeterWithoutErrors() {

    item = setUpCatalogueItem();
    when(catalogueService.findByModel("A0001")).thenReturn(Optional.of(item));

    Line l1 = new Line.Number("lineOne", NumberType.INTEGER, 3d);
    Line l2 = new Line.Number("lineTwo", NumberType.FLOAT, 9.999d);
    Line l3 = new Line.Text("lineThree", "Hello World!");
    Line l5 = new Line.ReadOnly("lineFive", "Read Only");

    Meter meter =
        MeterBuilder.builder()
            .locationId("location1")
            .model("A0001")
            .lines(List.of(l1, l2, l3, l5))
            .stage(ValidationStage.Connection)
            .build();
    MeterValidator validator = new MeterValidator(catalogueService);
    Errors errors = new Errors("meter");
    validator.validate(meter, errors);

    assertThat(errors.getErrorCount()).isZero();
  }

  @Test
  @DisplayName("A Meter without required lines should produce errors")
  void shouldErrorWithoutRequiredLines() {

    item = setUpCatalogueItem();
    when(catalogueService.findByModel("A0001")).thenReturn(Optional.of(item));

    Line l2 = new Line.Number("lineTwo", NumberType.FLOAT, 9.999d);
    Line l4 = new Line.Text("lineFour", "ABC");

    Meter meter =
        MeterBuilder.builder()
            .locationId("location1")
            .model("A0001")
            .lines(List.of(l2, l4))
            .stage(ValidationStage.Connection)
            .build();
    MeterValidator validator = new MeterValidator(catalogueService);
    Errors errors = new Errors("meter");
    validator.validate(meter, errors);

    assertThat(errors.getErrorCount()).isEqualTo(2);
    assertThat(errors.hasFieldError("lineOne")).isTrue();
    assertThat(errors.hasFieldError("lineThree")).isTrue();
    var err = errors.fieldError("lineOne");
    assertThat(err.errorCode().code()).isEqualTo("requiredField");
    assertThat(err.fieldContext()).isEqualTo("meter.lines");

    errors.fieldError("lineThree");
    assertThat(err.errorCode().code()).isEqualTo("requiredField");
    assertThat(err.fieldContext()).isEqualTo("meter.lines");
  }

  CatalogueItem setUpCatalogueItem() {

    Constraint<? extends Line> l1 =
        NumberLineConstraintBuilder.builder()
            .name("lineOne")
            .description("An integer between 2 and 5")
            .numberType(NumberType.INTEGER)
            .min(2d)
            .max(5d)
            .isRequired(true)
            .stage(ValidationStage.Connection)
            .build();

    Constraint<? extends Line> l2 =
        NumberLineConstraintBuilder.builder()
            .name("lineTwo")
            .description("A Double greater than 9.9")
            .numberType(NumberType.FLOAT)
            .min(9.9d)
            .isRequired(true)
            .stage(ValidationStage.Connection)
            .build();

    Constraint<? extends Line> l3 =
        TextLineConstraintBuilder.builder()
            .name("lineThree")
            .description("Text between 5 and 15 characters long")
            .minLength(5)
            .maxLength(15)
            .isRequired(true)
            .stage(ValidationStage.Connection)
            .build();

    Constraint<? extends Line> l4 =
        TextLineConstraintBuilder.builder()
            .name("lineFour")
            .description("Optional text less than or equal to 4 characters long")
            .maxLength(4)
            .isRequired(false)
            .stage(ValidationStage.Connection)
            .build();

    String model = "A0001";

    Level level = Level.DEVICE;

    MeterType meterType = MeterType.LORAWAN;

    String name = "SensorOne";

    String description = "Description of Sensor One";

    String manufacturer = "ManufacturerOne";

    return CatalogueItemBuilder.builder()
        .model(model)
        .level(level)
        .type(meterType)
        .description(description)
        .manufacturer(manufacturer)
        .constraints(List.of(l1, l2, l3, l4))
        .build();
  }
}
