package com.lumina.location.model;

import com.lumina.meter.model.Meter;
import io.soabase.recordbuilder.core.RecordBuilder;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@RecordBuilder
@Document(collection = "location")
@TypeAlias("Location")
public record Location(
        @Id String id,
        String name,
        String projectId,
        @ReadOnlyProperty
                @DocumentReference(lookup = "{'location':?#{#self.id}}", collection = "meterData")
                List<Meter> meters)
        implements LocationBuilder.With {

    public Location(String name, String projectId) {
        this(null, name, projectId, null);
    }
}
