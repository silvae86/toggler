package models.exchange;

import com.fasterxml.jackson.annotation.JsonAlias;
import database.MongoConfig;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import lombok.Getter;
import lombok.Setter;
import models.database.Service;
import models.roles.User;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

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

    @Property("user")
    private User creator;

    private HashSet<Service> allServices;

    public Config() {

    }

    public static Config getLatestConfig() {
        return MongoConfig.datastore().createQuery(Config.class)
                .order("-ts").limit(1).get();
    }

    public HashSet<Service> scanForServices() {
        HashSet<Service> allServices = new HashSet<>();
        for (ConfigNode cn : configNodes.values()) {
            allServices.addAll(cn.scanForServices());
        }

        return allServices;
    }

    public Config apply() throws Exception {

        allServices = scanForServices();
        for (String toggleName : this.getConfigNodes().keySet()) {
            ConfigNode node = configNodes.get(toggleName);
            node.setOwnerConfig(this);
            node.setToggleName(toggleName);
            node.apply(toggleName);
        }

        return this;
    }
}
