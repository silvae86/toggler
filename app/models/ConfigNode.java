package models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import play.libs.Json;

import java.util.HashSet;
import java.util.List;

@Entity("toggle_instances")
@Indexes({
        @Index(fields = {@Field("toggle.name"), @Field("service.name"), @Field("service.version")}, options = @IndexOptions(unique = true, dropDups = true)),
        @Index(fields = {@Field("service.name")})
})
@Getter
@Setter
public class ConfigNode {
    @Id
    private ObjectId id;

    // TODO replace with custom deserializer that requires a name in the root.
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Property("name")
    private String name;

    @Property("allow")
    @JsonAlias("allow")
    private HashSet<ServiceInstance> allow;

    @Property("deny")
    @JsonAlias("deny")
    private HashSet<ServiceInstance> deny;

    @Property("overrides")
    @JsonAlias("unless")
    private HashSet<ConfigNode> overrides;

    @JsonProperty("value")
    private Boolean defaultValue;

    public ConfigNode() {
    }

    public static List<ConfigNode> findByName(String name) {
        return MongoConfig.datastore().createQuery(ConfigNode.class)
                .field("name").equal(name)
                .asList();
    }

    public static List<ConfigNode> findByNameAndServiceName(String name, String serviceName) {
        return MongoConfig.datastore().createQuery(ConfigNode.class)
                .field("name").equal(name)
                .filter("service.name", serviceName)
                .asList();
    }

    public static ConfigNode findByNameServiceNameAndVersion(String name, String serviceName, String versionName) throws Exception {
        final List<ConfigNode> permissionNodes = MongoConfig.datastore().createQuery(ConfigNode.class)
                .field("name").equal(name)
                .filter("service.name", serviceName)
                .filter("service.version", serviceName)
                .asList();

        if (permissionNodes.size() == 1) {
            return permissionNodes.get(0);
        } else if (permissionNodes.size() == 0) {
            return null;
        } else {
            throw new Exception("More than one toggle with name " + name + ", applied to service " + serviceName + " and version " + versionName + " in the database!");
        }
    }

    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Json.toJson(this));
        } catch (JsonProcessingException e) {
            return ("Unable to serialize ConfigNode: " + e.getMessage());
        }
    }
}