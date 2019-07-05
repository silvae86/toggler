package models;

import com.fasterxml.jackson.annotation.JsonAlias;
import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import utils.PermissionsMap;

import java.util.Date;
import java.util.HashSet;

@Entity("permissions")
@Getter
@Setter
public class ConfigChange {
    @Id
    private ObjectId id;

    @Reference("toggles")
    @JsonAlias("toggles")
    private HashSet<PermissionNode> toggles;

    @Property("date_applied")
    private Date dateApplied;

    @Property("date_received")
    private Date dateReceived;

    @Reference("user")
    private User creator;

    private PermissionsMap permissions;

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

        this.permissions = new PermissionsMap();
        for (PermissionNode toggle : toggles) {
            permissions.combine(toggle.calculatePermissions());
        }

        this.dateApplied = new Date();
        MongoConfig.datastore().save(this);
    }
}
