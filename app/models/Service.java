package models;

import database.MongoConfig;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;

import java.util.HashMap;
import java.util.LinkedList;
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

    @Embedded("toggles")
    private LinkedList<Toggle> toggles;

    public static Service findByNameAndVersion(String name, String version)
    {
        Query<Service> query = MongoConfig.datastore().find(Service.class);
        query.or(
                query.criteria("name").equal(name),
                query.criteria("version").equal(version)
        );

        return query.get();
    }

    public Service(String name, String version)
    {
        this.name = name;
        this.version = version;
    }
}
