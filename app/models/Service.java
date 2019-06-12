package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity("services")
public class Service {
    @Id
    private ObjectId id;
    @Property("name")
    private String name;
}
