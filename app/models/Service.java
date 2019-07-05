package models;

import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;

import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
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
    private LinkedList<Toggle> toggles;

    public Service() {

    }

    public Service(String name, String version)
    {
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

    public void updateToggleValue(Toggle toggleToUpdate, Boolean newValue) {
        if (this.toggles.contains(toggleToUpdate)) {
            toggleToUpdate.setValue(newValue);
            MongoConfig.datastore().save(toggleToUpdate);
        } else {
            this.toggles.add(toggleToUpdate);
        }
    }

}
