package models;

import database.MongoConfig;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;

import java.util.HashSet;
import java.util.List;

@Entity("services")
@Indexes(@Index(fields = {@Field("name"), @Field("version")}, options = @IndexOptions(unique = true, dropDups = true)))
public class Service {
    @Id
    private ObjectId id;

    @Property("name")
    private String name;

    @Property("version")
    private String version;

    @Reference("toggles")
    private HashSet<Toggle> toggles;

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

    public void updateToggleValue(Toggle toggleToUpdate, Boolean newValue) {
        if (toggles.contains(toggleToUpdate)) {
            toggleToUpdate.setValue(newValue);
            MongoConfig.datastore().save(toggleToUpdate);
        } else {
            toggles.add(toggleToUpdate);
        }

    }

    public Service() {

    }

    public Service(String name, String version)
    {
        this.name = name;
        this.version = version;
    }
}
