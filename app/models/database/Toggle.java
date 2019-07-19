package models.database;

import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;

import java.util.Iterator;

@Entity("toggles")
@Indexes({
        @Index(fields = {
                @Field("name"),
                @Field("service.name"),
                @Field("service.version"),
                @Field("value")
        },
        options = @IndexOptions(unique = true, dropDups = true))
})
@Getter
@Setter
public class Toggle {
    @Id
    private ObjectId id;

    @Property("name")
    private String name;

    @Reference("service_name")
    private String serviceName;

    @Reference("service_version")
    private String serviceVersion;

    @Property("value")
    private Boolean value;

    public Toggle() {
    }

    public Toggle(
            String name,
            Service service,
            Boolean value
    ) {
        this.name = name;
        this.serviceName = service.getName();
        this.serviceVersion = service.getVersion();
        this.value = value;
    }

    public static Iterator<Toggle> findByName(String name) {
        Query<Toggle> togglesByNameQuery = MongoConfig.datastore().find(Toggle.class);
        togglesByNameQuery.criteria("name").equal(name);
        return togglesByNameQuery.iterator();
    }

    public static Iterator<Toggle> findByNameAndServiceName(String name, String serviceName) {
        Query<Toggle> togglesByNameAndServiceNameQuery = MongoConfig.datastore().find(Toggle.class);
        togglesByNameAndServiceNameQuery.and(
                togglesByNameAndServiceNameQuery.criteria("name").equal(name),
                togglesByNameAndServiceNameQuery.criteria("service_name").equal(serviceName)
        );

        return togglesByNameAndServiceNameQuery.iterator();
    }

    public static Toggle findByNameServiceNameAndVersion(String name, String serviceName, String serviceVersion) {
        Query<Toggle> togglesByNameQuery = MongoConfig.datastore().find(Toggle.class);
        togglesByNameQuery.and(
                togglesByNameQuery.criteria("name").equal(name),
                togglesByNameQuery.criteria("service_name").equal(serviceName),
                togglesByNameQuery.criteria("service_version").equal(serviceVersion)
        );

        return togglesByNameQuery.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Toggle))
            return false;
        if (obj == this)
            return true;

        Toggle s = (Toggle) obj;
        if (this.name.equals(s.name)) {
            if (this.serviceName != null) {
                if (s.serviceVersion != null) {
                    return this.serviceVersion.equals(s.serviceVersion);
                } else {
                    return false;
                }
            } else {
                return s.serviceVersion == null;
            }
        } else {
            return false;
        }
    }
}
