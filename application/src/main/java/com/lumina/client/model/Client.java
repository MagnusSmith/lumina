package com.lumina.client.model;

import com.lumina.project.model.Project;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@RecordBuilder
@Document(collection = "client")
@TypeAlias("Client")
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
