package com.lumina.client.dto;

import com.lumina.client.model.Client;
import com.lumina.project.dto.ProjectDto;
import java.util.List;

public record ClientDto(String id, String name, List<ProjectDto> projects) {

    public static ClientDto from(Client client) {
        return new ClientDto(
                client.id(), client.name(), client.projects().stream().map(ProjectDto::from).toList());
    }
}
