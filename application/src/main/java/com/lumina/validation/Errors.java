package com.lumina.validation;

import io.micrometer.common.util.StringUtils;
import java.util.*;

public class Errors {
    private final List<Error> fieldErrors = new ArrayList<>();

    private String context = "";

    public Errors(String context) {
        this.context = context;
    }

    public String pushContext(String subContext) {
        context = context.isEmpty() ? subContext : STR."\{context}.\{subContext}";
        return context;
    }

    public String popContext() {
        context = context.substring(0, context.lastIndexOf("."));
        return context;
    }

    public String context() {
        return context;
    }

    public Errors add(Error error) {
        fieldErrors.add(error.withFieldContext(context()));
        return this;
    }

    public Errors rejectIfEmpty(String field, Object value, ErrorCode errorCode) {
        if (Objects.isNull(value) || StringUtils.isBlank(value.toString())) {
            add(ErrorBuilder.builder().field(field).rejectedValue(value).errorCode(errorCode).build());
        }
        return this;
    }

    public Errors rejectValue(String field, ErrorCode errorCode) {
        add(ErrorBuilder.builder().field(field).errorCode(errorCode).build());
        return this;
    }

    public int getErrorCount() {
        return fieldErrors.size();
    }

    public boolean hasFieldError(String name) {
        return fieldErrors.stream().anyMatch(f -> f.field().equals(name));
    }

    public Error fieldError(String name) {
        return fieldErrors.stream().filter(f -> f.field().equals(name)).findFirst().orElseThrow();
    }

    public Set<Error> fieldErrors() {
        return new HashSet<>(fieldErrors);
    }
}
