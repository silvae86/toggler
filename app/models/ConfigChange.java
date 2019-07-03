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
    private String content;

    @Property("date_applied")
    private Date dateApplied;

    @Property("date_received")
    private Date dateReceived;

    @Reference("user")
    private User creator;

    public ConfigChange()
    {

    }

    public ConfigChange(String content)
    {
        this.content = content;
        this.dateReceived = new Date();
    }

    public static ConfigChange getLatestConfig()
    {
        return MongoConfig.datastore().createQuery(ConfigChange.class)
                .order("-ts").limit(1).get();
    }

    public void apply() {
        this.dateApplied = new Date();
        MongoConfig.datastore().save(this);
    }
}
