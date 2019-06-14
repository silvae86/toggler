package models;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

@Entity("toggles")

@Indexes(
        @Index(fields = @Field("name"))
)

@Getter
@Setter
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