package com.lumina.catalogue.defaults;

import com.lumina.catalogue.model.constraint.Constraint;
import com.lumina.catalogue.model.constraint.TextLineConstraintBuilder;
import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Line;

import java.util.List;

public class AwsThing {
  static List<Constraint<? extends Line>> constraints() {

    Constraint<? extends Line> awsThingTypeName =
        TextLineConstraintBuilder.builder()
            .name("aws.thingTypeName")
            .description("AWS ThingTypeName")
            .minLength(5)
            .isRequired(false)
            .stage(ValidationStage.Two)
            .build();

    Constraint<? extends Line> awsThingGroup =
        TextLineConstraintBuilder.builder()
            .name("aws.thingGroup")
            .description("The AWS ThingGroup")
            .minLength(5)
            .isRequired(false)
            .stage(ValidationStage.Two)
            .build();

    Constraint<? extends Line> awsThingId =
        TextLineConstraintBuilder.builder()
            .name("aws.thingId")
            .description("AWS ThingId")
            .minLength(5)
            .isRequired(false)
            .stage(ValidationStage.Two)
            .build();

    Constraint<? extends Line> awsThingArn =
        TextLineConstraintBuilder.builder()
            .name("aws.thingArn")
            .description("AWS ThingARN")
            .minLength(5)
            .isRequired(false)
            .stage(ValidationStage.Two)
            .build();

    Constraint<? extends Line> awsDestinationName =
        TextLineConstraintBuilder.builder()
            .name("aws.destinationName")
            .description("AWS Destination Name - Device Routing")
            .minLength(0)
            .maxLength(0)
            .isRequired(false)
            .stage(ValidationStage.Two)
            .build();

    return List.of(awsThingTypeName, awsThingGroup, awsThingId, awsThingArn, awsDestinationName);
  }
}
