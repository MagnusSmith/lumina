package com.lumina.catalogue.defaults;

import com.lumina.catalogue.model.*;
import com.lumina.catalogue.model.constraint.Constraint;
import com.lumina.catalogue.model.constraint.NumberLineConstraintBuilder;
import com.lumina.catalogue.model.constraint.TextLineConstraintBuilder;
import com.lumina.meter.model.Line;

import java.util.ArrayList;
import java.util.List;

public class LorawanGateway {

  public static Preset preset(){
    return PresetBuilder.builder().type(MeterType.LORAWAN).level(Level.GATEWAY).constraints(constraints()).build();
  }

  static List<Constraint<? extends Line>> constraints() {

    Constraint<? extends Line> name =
        TextLineConstraintBuilder.builder()
            .name("name")
            .description("The name of the device")
            .minLength(0)
            .maxLength(75)
            .stage(ValidationStage.Connection)
            .isRequired(false)
            .build();

    Constraint<? extends Line> lnsUri =
        TextLineConstraintBuilder.builder()
            .name("lnsUri")
            .description("The LNS URI")
            .minLength(50)
            .maxLength(80)
            .stage(ValidationStage.Connection)
            .isRequired(false)
            .build();

    Constraint<? extends Line> publicKey =
        TextLineConstraintBuilder.builder()
            .name("cupsTrust")
            .description("The CUPS Trust")
            .minLength(50)
            .maxLength(80)
            .isRequired(false)
            .stage(ValidationStage.Connection)
            .build();

    Constraint<? extends Line> lnsTrust =
        TextLineConstraintBuilder.builder()
            .name("lnsTrust ")
            .description("The LNS Trust")
            .minLength(0)
            .maxLength(16)
            .isRequired(false)
            .stage(ValidationStage.Connection)
            .build();

    Constraint<? extends Line> privateKey =
        TextLineConstraintBuilder.builder()
            .name("privateKey")
            .description("The Device Private Key")
            .minLength(0)
            .maxLength(16)
            .isRequired(false)
            .stage(ValidationStage.Connection)
            .build();

    Constraint<? extends Line> pemCert =
        TextLineConstraintBuilder.builder()
            .name("pemCert")
            .description("Device PEM certificate")
            .minLength(0)
            .maxLength(16)
            .isRequired(false)
            .stage(ValidationStage.Connection)
            .build();

// HEXADECIMAL
    Constraint<? extends Line> devEui =
        TextLineConstraintBuilder.builder()
            .name("lorawan.devEui")
            .description("LoRaWAN Device EUI")
            .minLength(16)
            .maxLength(16)
            .isRequired(true)
            .stage(ValidationStage.Connection)
            .build();


    Constraint<? extends Line> comInterval =
        NumberLineConstraintBuilder.builder()
            .name("communicationInterval")
            .description("The number of seconds between readings")
            .numberType(NumberType.INTEGER)
            .min(1d)
            .isRequired(false)
            .stage(ValidationStage.Connection)
            .build();



    Constraint<? extends Line> cupsUri =
        TextLineConstraintBuilder.builder()
            .name("cupsUri")
            .description("The CUPS URI")
            .minLength(128)
            .isRequired(false)
            .stage(ValidationStage.Connection)
            .build();


    Constraint<? extends Line> freqBand =
        TextLineConstraintBuilder.builder()
            .name("lorawan.freqBand")
            .description("LoRaWAN Frequency Band")
            .minLength(5)
            .isRequired(false)
            .stage(ValidationStage.Connection)
            .build();

    Constraint<? extends Line> awsId =
        TextLineConstraintBuilder.builder()
            .name("lorawan.awsId")
            .description("LoRaWAN Wireless device Id")
            .minLength(3)
            .maxLength(36)
            .isRequired(false)
            .stage(ValidationStage.Connection)
            .build();

    var f =
        new ArrayList<>(List.of(
            name,
            comInterval,
            pemCert,
            publicKey,
            privateKey,
            lnsTrust,
            lnsUri,
            cupsUri,
            devEui,
            freqBand,
            awsId));

    f.addAll(AwsThing.constraints());
    return f;
  }
}
