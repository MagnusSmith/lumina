package com.lumina.project.dto;

import com.lumina.project.model.Project;
import com.lumina.project.model.ProjectBuilder;

public record UpdateProjectDto(String id, String clientId, String name, String billingGroup) {
    public static Project toModel(UpdateProjectDto updateProjectDto) {
        return ProjectBuilder.builder()
                .id(updateProjectDto.id())
                .clientId(updateProjectDto.clientId())
                .name(updateProjectDto.name())
                .billingGroup(updateProjectDto.billingGroup())
                .build();
    }
}
