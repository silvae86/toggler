package models.concepts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import models.ConfigNode;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import play.libs.Json;

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

    @Reference("value")
    private boolean value;
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Reference("permission_nodes")
    private HashSet<ConfigNode> toggles;

    public Service(String name) {
        this.name = name;
    }

    public Service(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public Service() {
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

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    public Boolean equals(Service s) {
        if (this.name.equals(s.name)) {
            if (this.version != null) {
                if (s.version != null) {
                    return this.version.equals(s.version);
                } else {
                    return false;
                }
            } else {
                return s.version == null;
            }
        } else {
            return false;
        }
    }

    public boolean moreSpecificThan(Service service) {
        if (this.getName().equals(service.getName())) {
            if (this.getVersion() == null && service.getVersion() != null) {
                return false;
            } else if (this.getVersion() != null && service.getVersion() == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Json.toJson(this));
        } catch (JsonProcessingException e) {
            return ("Unable to serialize Service: " + e.getMessage());
        }
    }
}
