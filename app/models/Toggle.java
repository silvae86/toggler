package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

@Entity("toggles")

@Indexes(
        @Index(fields = @Field("name"))
)

public class Toggle {
    @Id
    private ObjectId id;

    @Property("name")
    private String name;

    @Property("value")
    private boolean value;

    @Reference
    public Permission permissionRequired;

    public Toggle(String name, boolean value) {
        this.name = name;
        this.value = value;
    }
}