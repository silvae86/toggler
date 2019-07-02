package models;

import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.Date;

@Entity("permissions")
@Getter
@Setter
public class ConfigChange {
    @Id
    private ObjectId id;

    @Property("content")
    public String content;

    @Reference("user")
    public User creator;

    @Reference("date_applied")
    public Date dateApplied;

    @Reference("date_received")
    public Date dateReceived;


    public ConfigChange()
    {

    }

    public ConfigChange(String content)
    {
        this.content = content;
    }

    public static ConfigChange getLatestConfig()
    {
        return MongoConfig.datastore().createQuery(ConfigChange.class)
                .order("-ts").limit(1).get();
    }

}
