package models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import utils.PermissionsMap;

import java.util.HashSet;
import java.util.List;

@Entity("toggle_instances")
@Indexes({
        @Index(fields = {@Field("toggle.name"), @Field("service.name"), @Field("service.version")}, options = @IndexOptions(unique = true, dropDups = true)),
        @Index(fields = {@Field("service.name")})
})
@Getter
@Setter
public class PermissionNode {
    @Id
    private ObjectId id;

    // TODO replace with custom deserializer that requires a name in the root.
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Property("name")
    private String name;

    @Property("allow")
    @JsonAlias("allow")
    private HashSet<Toggle> allow;

    @Property("deny")
    @JsonAlias("deny")
    private HashSet<Toggle> deny;

    @Property("value")
    @JsonAlias("value")
    private Boolean defaultValue;

    @Property("overrides")
    @JsonAlias("unless")
    private HashSet<PermissionNode> overrides;

    public PermissionNode() {
    }

    public static List<PermissionNode> findByName(String name) {
        return MongoConfig.datastore().createQuery(PermissionNode.class)
                .field("name").equal(name)
                .asList();
    }

    public static List<PermissionNode> findByNameAndServiceName(String name, String serviceName) {
        return MongoConfig.datastore().createQuery(PermissionNode.class)
                .field("name").equal(name)
                .filter("service.name", serviceName)
                .asList();
    }

    public static PermissionNode findByNameServiceNameAndVersion(String name, String serviceName, String versionName) throws Exception {
        final List<PermissionNode> permissionNodes = MongoConfig.datastore().createQuery(PermissionNode.class)
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

    private PermissionsMap calculatePermissionsHelper(PermissionsMap map) {
        map.combine(new PermissionsMap(this.allow, this.deny));

        for (PermissionNode override : this.overrides) {
            map.combine(new PermissionsMap(override.allow, override.deny));
        }

        return map;
    }

    public PermissionsMap calculatePermissions() {
        PermissionsMap toReturn = new PermissionsMap();
        return calculatePermissionsHelper(toReturn);
    }
}