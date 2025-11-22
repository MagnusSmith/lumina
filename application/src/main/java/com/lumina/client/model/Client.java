package com.lumina.client.model;

import com.lumina.project.model.Project;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@RecordBuilder
@Document(collection = "client")
@TypeAlias("Client")
public record Client(
    @Id String id,
    String name,
    @ReadOnlyProperty @DocumentReference(lookup = "{'project':?#{#self.id}}")
        List<Project> projects,
    @CreatedDate Instant createdAt,
    @LastModifiedDate Instant updatedAt,
    @CreatedBy String createdBy,
    @LastModifiedBy String updatedBy)
    implements ClientBuilder.With {

  public Client(String name) {
    this(null, name, null, null, null, null, null);
  }
}
