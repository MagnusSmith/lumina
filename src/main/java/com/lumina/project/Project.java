package com.lumina.project;

import com.lumina.location.Location;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

import static com.lumina.project.ProjectBuilder.*;

@RecordBuilder
@Document
public record Project(

    @Id String id,

    String clientId,
    String name,

    @ReadOnlyProperty
    @DocumentReference(lookup="{'location':?#{#self.id}}")
    List<Location> locations
) implements With {}
