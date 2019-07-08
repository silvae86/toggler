package models;

import com.fasterxml.jackson.annotation.JsonAlias;
import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import models.roles.User;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.Date;
import java.util.HashSet;

@Entity("permissions")
@Getter
@Setter
public class Config {
    @Id
    private ObjectId id;

    @Reference("permission_nodes")
    @JsonAlias("toggles")
    private HashSet<ConfigNode> permissionNodes;

    @Property("date_applied")
    private Date dateApplied;

    @Property("date_received")
    private Date dateReceived;

    @Reference("user")
    private User creator;

    public Config()
    {

    }

    public static Config getLatestConfig()
    {
        return MongoConfig.datastore().createQuery(Config.class)
                .order("-ts").limit(1).get();
    }
}
