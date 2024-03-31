package com.lumina.client;

import com.lumina.project.Project;
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

//  public Client {
//    clientId = Objects.requireNonNullElse(clientId, new ClientId(0L));
//    }
//
//    public Client(String name, List<Project> projects){
//        this(new ClientId(0L), name, projects);
//    }
//
//    @Transient
//    public static final String SEQUENCE_NAME = "clientData_sequence";

}
