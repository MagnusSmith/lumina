package com.lumina.project.model;

import com.lumina.location.model.Location;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

import static com.lumina.project.model.ProjectBuilder.*;

@RecordBuilder
@Document
public record Project(

    @Id String id,

    String clientId,
    String name,

    @ReadOnlyProperty
    @DocumentReference(lookup="{'location':?#{#self.id}}")
    List<Location> locations
) implements With {
    Project(String clientId, String name){
        this(null, clientId, name, null);
    }
}
