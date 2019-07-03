package models;

import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.List;

@Entity("toggles")

@Indexes(
        @Index(fields = {@Field("name")})
)

@Getter
@Setter
public class Toggle {
    @Id
    private ObjectId id;

    @Property("name")
    private String name;

    @Property("value")
    private Boolean value;

    @Reference("service")
    private Service service;

    public Toggle(){}

    public Toggle(String name) {
        this.name = name;
    }

    public static List<Toggle> findByName(String name) {
        return MongoConfig.datastore().createQuery(Toggle.class)
                .field("name").equal(name)
                .asList();
    }

    public static List<Toggle> findByNameAndServiceName(String name, String serviceName) {
        return MongoConfig.datastore().createQuery(Toggle.class)
                .field("name").equal(name)
                .filter("service.name", serviceName)
                .asList();
    }

    public static Toggle findByNameServiceNameAndVersion(String name, String serviceName, String versionName) throws Exception {
        final List<Toggle> toggles = MongoConfig.datastore().createQuery(Toggle.class)
                .field("name").equal(name)
                .filter("service.name", serviceName)
                .filter("service.version", serviceName)
                .asList();

        if (toggles.size() == 1) {
            return toggles.get(0);
        } else if (toggles.size() == 0) {
            return null;
        } else {
            throw new Exception("More than one toggle with name " + name + ", applied to service " + serviceName + " and version " + versionName + " in the database!");
        }
    }
}