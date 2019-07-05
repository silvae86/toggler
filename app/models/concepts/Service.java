package models.concepts;

import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import models.ToggleInstance;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;

import java.util.HashSet;
import java.util.List;

@Entity("services")
@Indexes(@Index(fields = {@Field("name"), @Field("version")}, options = @IndexOptions(unique = true, dropDups = true)))
@Getter
@Setter
public class Service {
    @Id
    private ObjectId id;

    @Property("name")
    private String name;

    @Property("version")
    private String version;

    @Reference("toggles")
    private HashSet<ToggleInstance> toggles;

    public Service() {

    }

    public Service(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public static Service findByNameAndVersion(String name, String version)
    {
        Query<Service> query = MongoConfig.datastore().find(Service.class);
        query.and(
                query.criteria("name").equal(name),
                query.criteria("version").equal(version)
        );

        return query.get();
    }

    public static List<Service> findByName(String name) {
        Query<Service> query = MongoConfig.datastore().find(Service.class);
        query.criteria("name").equal(name);
        return query.asList();
    }
}