package models;

import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.Date;
import java.util.LinkedList;

@Entity("permissions")
@Getter
@Setter
public class ConfigChange {
    @Id
    private ObjectId id;

    @Embedded("services")
    private LinkedList<Service> services;

    @Embedded("toggles")
    private LinkedList<Toggle> toggles;

    @Property("date_applied")
    private Date dateApplied;

    @Property("date_received")
    private Date dateReceived;

    @Reference("user")
    private User creator;

    @Property("allow_access")
    private Boolean allow_access;

    public ConfigChange()
    {

    }

    public ConfigChange(String content)
    {
        this.dateReceived = new Date();
    }

    public static ConfigChange getLatestConfig()
    {
        return MongoConfig.datastore().createQuery(ConfigChange.class)
                .order("-ts").limit(1).get();
    }

    public void apply() {
        this.dateReceived = new Date();
        MongoConfig.datastore().save(this);


    }
}
