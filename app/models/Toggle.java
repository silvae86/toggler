package models;

import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.List;

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

    public Toggle(){}

    public Toggle(String name, boolean value) {
        this.name = name;
        this.value = value;
    }

    public static Toggle findByName(String name) throws Exception
    {
        final List<Toggle> toggles = MongoConfig.datastore().createQuery(Toggle.class)
                .field("name").equal(name)
                .asList();

        if(toggles.size() == 1)
        {
            return toggles.get(0);
        }
        else if(toggles.size() == 0)
        {
            return null;
        }
        else
        {
            throw new Exception("More than one toggle with name " + name + " present in the database!");
        }
    }
}