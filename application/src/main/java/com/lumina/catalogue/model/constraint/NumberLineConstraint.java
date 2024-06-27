package com.lumina.catalogue.model.constraint;

import static com.lumina.validation.ErrorCode.*;

import com.lumina.catalogue.model.NumberType;
import com.lumina.catalogue.model.ValidationStage;
import com.lumina.meter.model.Line;
import com.lumina.validation.ErrorBuilder;
import com.lumina.validation.Errors;
import com.lumina.validation.ValidationStageEnum;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@RecordBuilder
public record NumberLineConstraint(
        String name,
        String description,
        NumberType numberType,
        Double min,
        Double max,
        boolean isRequired,
        @ValidationStageEnum ValidationStage stage)
        implements Constraint<Line.Number> {

    public void validate(Line.Number line, Errors errors, ValidationStage validationStage) {
        if (stage().shouldValidateAt(validationStage)) {
            var value = line.value();
            if (value instanceof Double valD) {
                if (numberType == NumberType.INTEGER) {
                    if ((valD == Math.floor(valD)) && !Double.isInfinite(valD)) {
                        // integer type
                        int valInt = valD.intValue();
                        if (Objects.nonNull(min) && min.intValue() > valInt) {
                            errors.add(
                                    ErrorBuilder.builder()
                                            .field(name)
                                            .errorCode(LESS_THAN)
                                            .errorCodeArgs(new Object[] {valInt, min.intValue()})
                                            .rejectedValue(valInt)
                                            .build());
                        } else if (Objects.nonNull(max) && max.intValue() < valInt) {
                            errors.add(
                                    ErrorBuilder.builder()
                                            .field(name)
                                            .errorCode(GREATER_THAN)
                                            .errorCodeArgs(new Object[] {valInt, max.intValue()})
                                            .rejectedValue(valInt)
                                            .build());
                        }
                    } else {
                        errors.add(
                                ErrorBuilder.builder()
                                        .field(name)
                                        .errorCode(NOT_INTEGER)
                                        .errorCodeArgs(new Object[] {valD})
                                        .rejectedValue(valD)
                                        .build());
                    }
                }
                if (numberType == NumberType.FLOAT) {
                    if (Objects.nonNull(min) && min > valD) {
                        errors.add(
                                ErrorBuilder.builder()
                                        .field(name)
                                        .errorCode(LESS_THAN)
                                        .errorCodeArgs(new Object[] {valD, min})
                                        .rejectedValue(valD)
                                        .build());

                    } else if (Objects.nonNull(max) && max < valD) {
                        errors.add(
                                ErrorBuilder.builder()
                                        .field(name)
                                        .errorCode(GREATER_THAN)
                                        .errorCodeArgs(new Object[] {valD, max})
                                        .rejectedValue(valD)
                                        .build());
                    }
                }
            }
        }
    }
}
