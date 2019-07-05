package models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.HashSet;
import java.util.List;

@Entity("toggle_instances")
@Indexes({
        @Index(fields = {@Field("toggle.name"), @Field("service.name"), @Field("service.version")}, options = @IndexOptions(unique = true, dropDups = true)),
        @Index(fields = {@Field("service.name")})
})
@Getter
@Setter
public class ToggleInstance {
    @Id
    private ObjectId id;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Property("name")
    private String name;

    @Property("allow")
    @JsonAlias("allow")
    private HashSet<ServiceToggle> allowedServices;

    @Property("deny")
    @JsonAlias("deny")
    private HashSet<ServiceToggle> deniedServices;

    @Property("value")
    @JsonAlias("value")
    private Boolean defaultValue;

    @Property("overrides")
    @JsonAlias("unless")
    private HashSet<ToggleInstance> overrides;

    public ToggleInstance() {
    }

    public static List<ToggleInstance> findByName(String name) {
        return MongoConfig.datastore().createQuery(ToggleInstance.class)
                .field("name").equal(name)
                .asList();
    }

    public static List<ToggleInstance> findByNameAndServiceName(String name, String serviceName) {
        return MongoConfig.datastore().createQuery(ToggleInstance.class)
                .field("name").equal(name)
                .filter("service.name", serviceName)
                .asList();
    }

    public static ToggleInstance findByNameServiceNameAndVersion(String name, String serviceName, String versionName) throws Exception {
        final List<ToggleInstance> toggleInstances = MongoConfig.datastore().createQuery(ToggleInstance.class)
                .field("name").equal(name)
                .filter("service.name", serviceName)
                .filter("service.version", serviceName)
                .asList();

        if (toggleInstances.size() == 1) {
            return toggleInstances.get(0);
        } else if (toggleInstances.size() == 0) {
            return null;
        } else {
            throw new Exception("More than one toggle with name " + name + ", applied to service " + serviceName + " and version " + versionName + " in the database!");
        }
    }

    public void getPermissionsMatrix() {
        allowedServices.remove
    }
}