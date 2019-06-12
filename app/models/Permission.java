package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;

import java.util.LinkedList;

@Entity("permissions")
public class Permission {
    @Id
    private ObjectId id;

    @Reference
    public Permission parent;

    @Reference
    public LinkedList<Service> services;

    @Property("allow_or_refuse")
    public boolean allowOrRefuse;

    @Property("type")
    public String type;
}
