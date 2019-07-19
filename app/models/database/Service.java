package models.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.MongoConfig;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import play.libs.Json;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Entity("services")
@Indexes(
        {
                @Index(fields = {@Field("name"), @Field("version")}, options = @IndexOptions(unique = true, dropDups = true)),
                @Index(fields = {@Field("toggles.name"), @Field("toggles.value")}, options = @IndexOptions(unique = true, dropDups = true))
        })
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Service {

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Id
    private ObjectId id;

    @Property("name")
    private String name;

    @Property("version")
    private String version;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Embedded("toggles")
    private HashSet<Toggle> toggles = new HashSet<>();

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
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Service))
            return false;
        if (obj == this)
            return true;

        Service s = (Service) obj;
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

    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Json.toJson(this));
        } catch (JsonProcessingException e) {
            return ("Unable to serialize Service: " + e.getMessage());
        }
    }

    public void addOrUpdateToggle(Toggle toggle) {
        Iterator<Toggle> existingToggles;

        if (this.version == null) {
            existingToggles = Toggle.findByNameAndServiceName(toggle.getName(), this.name);
        } else {
            existingToggles = Arrays.asList(Toggle.findByNameServiceNameAndVersion(toggle.getName(), this.name, this.version)).iterator();
        }

        boolean toggleExists = (existingToggle != null);

        if (toggleExists) {
            existingToggle.setValue(toggle.getValue());
            MongoConfig.datastore().save(existingToggle);
        } else {
            boolean existsInMemory = false;
            for (Iterator<Toggle> toggleIterator = toggles.iterator(); toggleIterator.hasNext(); ) {
                Toggle aToggle = toggleIterator.next();
                if (aToggle.getName().equals(toggle.getName())) {
                    existsInMemory = true;
                }
            }

            if (!existsInMemory) {
                toggles.add(toggle);
            }

            MongoConfig.datastore().save(this);
        }
    }

    public void removeToggle(Toggle toggleToRemove) {
        Toggle existingToggle = Toggle.findByNameServiceNameAndVersion(toggleToRemove.getName(), this.getName(), this.getVersion());
        boolean toggleExists = (existingToggle != null);

        if (toggleExists) {
            MongoConfig.datastore().delete(existingToggle);
        }
    }

    public boolean canAccess(Toggle t)
    {
        return true;
    }
}
