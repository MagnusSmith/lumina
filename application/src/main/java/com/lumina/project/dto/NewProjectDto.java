package com.lumina.project.dto;

import com.lumina.project.model.Project;
import com.lumina.project.model.ProjectBuilder;

public record NewProjectDto(String clientId, String name) {

    public static Project toModel(NewProjectDto newProjectDto) {
        return ProjectBuilder.builder()
                .clientId(newProjectDto.clientId())
                .name(newProjectDto.name())
                .build();
    }
}
