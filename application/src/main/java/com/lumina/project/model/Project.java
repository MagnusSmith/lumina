package com.lumina.project.model;

import static com.lumina.project.model.ProjectBuilder.*;

import com.lumina.location.model.Location;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@RecordBuilder
@Document(collection = "project")
@TypeAlias("Project")
public record Project(
    @Id String id,
    String clientId,
    String name,
    String billingGroup,
    @ReadOnlyProperty @DocumentReference(lookup = "{'location':?#{#self.id}}")
        List<Location> locations)
    implements With {
  Project(String clientId, String name, String billingGroup) {
    this(null, clientId, name, billingGroup, null);
  }
}
