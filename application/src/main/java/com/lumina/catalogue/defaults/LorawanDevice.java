package com.lumina.catalogue.defaults;

import com.lumina.catalogue.model.*;
import com.lumina.catalogue.model.constraint.Constraint;
import com.lumina.catalogue.model.constraint.TextLineConstraintBuilder;
import com.lumina.meter.model.Line;
import java.util.ArrayList;
import java.util.List;

public class LorawanDevice {

    public static Preset preset() {
        return PresetBuilder.builder()
                .type(MeterType.LORAWAN)
                .level(Level.DEVICE)
                .constraints(constraints())
                .build();
    }

    static List<Constraint<? extends Line>> constraints() {

        Constraint<? extends Line> devEui =
                TextLineConstraintBuilder.builder()
                        .name("lorawan.devEui")
                        .description("LoRaWAN Dev Eui ??")
                        .minLength(16)
                        .isRequired(false)
                        .stage(ValidationStage.Connection)
                        .build();

        Constraint<? extends Line> appEui =
                TextLineConstraintBuilder.builder()
                        .name("lorawan.appEui")
                        .description("LoRaWAN App Eui ??")
                        .minLength(16)
                        .isRequired(false)
                        .stage(ValidationStage.Connection)
                        .build();

        Constraint<? extends Line> appKey =
                TextLineConstraintBuilder.builder()
                        .name("lorawan.appKey")
                        .description("LoRaWAN App Key")
                        .minLength(16)
                        .isRequired(false)
                        .stage(ValidationStage.Connection)
                        .build();

        Constraint<? extends Line> deviceProfileId =
                TextLineConstraintBuilder.builder()
                        .name("lorawan.deviceProfileId")
                        .description("LoRaWAN Device Profile Id")
                        .minLength(16)
                        .isRequired(false)
                        .stage(ValidationStage.Connection)
                        .build();

        Constraint<? extends Line> serviceProfileId =
                TextLineConstraintBuilder.builder()
                        .name("lorawan.serviceProfileId")
                        .description("LoRaWAN Service Profile Id")
                        .minLength(16)
                        .isRequired(false)
                        .stage(ValidationStage.Connection)
                        .build();

        Constraint<? extends Line> awsId =
                TextLineConstraintBuilder.builder()
                        .name("lorawan.awsId")
                        .description("LoRaWAN AWS Id")
                        .minLength(5)
                        .isRequired(false)
                        .stage(ValidationStage.Connection)
                        .build();

        var f =
                new ArrayList<>(List.of(devEui, appEui, appKey, deviceProfileId, serviceProfileId, awsId));
        f.addAll(AwsThing.constraints());
        return f;
    }
}
