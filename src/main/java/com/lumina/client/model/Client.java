package com.lumina.client.model;

import com.lumina.client.model.ClientBuilder;
import com.lumina.project.model.Project;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@RecordBuilder
@Document(collection = "clientData")
public record Client (

    @Id
    String id,

    String name,

    @ReadOnlyProperty
    @DocumentReference(lookup="{'project':?#{#self.id}}")
    List<Project> projects
    ) implements ClientBuilder.With {

    public Client(String name){
        this(null, name, null);
    }


}
