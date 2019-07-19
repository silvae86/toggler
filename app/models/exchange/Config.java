package models.exchange;

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
import java.util.HashMap;

@Entity("configurations")
@Getter
@Setter
public class Config {
    @Id
    private ObjectId id;

    @Property("toggles")
    @JsonAlias("toggles")
    private HashMap<String, ConfigNode> configNodes;

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

    public Config apply() {
        for (String toggleName : this.getConfigNodes().keySet()) {
            configNodes.get(toggleName).apply(toggleName);
        }

        return this;
    }
}
