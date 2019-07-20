package models.database;

import database.MongoConfig;
import dev.morphia.annotations.*;
import dev.morphia.query.Query;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.Iterator;

@Entity("toggles")
@Indexes({
        @Index(fields = {
                @Field("name"),
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

    @Property("value")
    private Boolean value;

    public Toggle() {
    }

    public Toggle(
            String name,
            Boolean value
    ) {
        this.name = name;
        this.value = value;
    }

    // for finding toggle instances inside services

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
}
