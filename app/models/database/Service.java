package models.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.MongoConfig;
import dev.morphia.annotations.*;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import play.libs.Json;

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
    @Property("toggles")
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

    public static Service find(Service service) {
        Query<Service> query = MongoConfig.datastore().find(Service.class);
        query.criteria("name").equal(service.getName());

        if (service.getVersion() != null) {
            query.criteria("version").equal(service.getVersion());
        }

        Service persistedService = query.first();

        if (persistedService != null)
            return persistedService;
        else
            return service;
    }

    public static Service findByNameAndVersion(String name, String version) {
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
        return (this.name + "_" + ((this.version == null) ? "" : "_" + this.version)).hashCode();
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

        // Get query to retrieve this service
        Query<Service> serviceWithNameAndToggleQuery = MongoConfig.datastore().createQuery(Service.class)
                .field("name").equal(this.getName());

        if (this.getVersion() != null) {
            serviceWithNameAndToggleQuery.field("version").equal(this.version);
        }

        if (toggleExists(toggle)) {
            serviceWithNameAndToggleQuery.field("toggles.name").equal(toggle.getName());

            UpdateOperations<Service> setToggleValueOperation = MongoConfig.datastore().
                    createUpdateOperations(Service.class).set("value", toggle.getValue());

            MongoConfig.datastore().findAndModify(serviceWithNameAndToggleQuery, setToggleValueOperation);
        } else {
            final UpdateOperations<Service> toggleInsertOperation = MongoConfig.datastore()
                    .createUpdateOperations(Service.class)
                    .push("toggles", toggle);

            MongoConfig.datastore().findAndModify(serviceWithNameAndToggleQuery, toggleInsertOperation);
        }
    }

    public void removeToggles(Iterator<Toggle> togglesToRemove) {
        while (togglesToRemove.hasNext()) {
            Toggle toggle = togglesToRemove.next();
            removeToggle(toggle);
        }
    }

    public void addOrUpdateToggles(Iterator<Toggle> togglesToAdd) {
        while (togglesToAdd.hasNext()) {
            Toggle toggle = togglesToAdd.next();
            addOrUpdateToggle(toggle);
        }
    }

    public void removeToggle(Toggle toggleToRemove) {
        boolean toggleExists = toggleExists(toggleToRemove);
        if (toggleExists) {
            Query<Service> serviceWithNameAndToggleQuery = MongoConfig.datastore().find(Service.class);
            serviceWithNameAndToggleQuery.and(
                    serviceWithNameAndToggleQuery
                            .criteria("name").equal(this.getName())
                            .criteria("toggles.name").equal(toggleToRemove.getName())
            );

            if (this.getVersion() != null) {
                serviceWithNameAndToggleQuery.field("version").equal(this.getVersion());
            }

            toggleToRemove.setValue(null);

            UpdateOperations<Service> toggleRemoveOperation = MongoConfig.datastore().createUpdateOperations(Service.class)
                    .removeAll("toggles", toggleToRemove);

            MongoConfig.datastore().findAndModify(serviceWithNameAndToggleQuery, toggleRemoveOperation);
        }
    }

    public boolean canAccess(Toggle t) {
        return toggleExists(t);
    }

    private boolean toggleExists(Toggle toggle) {
        Query<Service> serviceWithNameAndToggleQuery = MongoConfig.datastore().find(Service.class);
        serviceWithNameAndToggleQuery
                .filter("toggles.name", toggle.getName())
                .filter("name", this.name);

        if (this.version != null) {
            serviceWithNameAndToggleQuery.filter("version", this.version);
        }

        return serviceWithNameAndToggleQuery.find().hasNext();
    }

    private Boolean getToggleValue(Toggle toggle) {
        Query<Service> getToggleValueQuery = MongoConfig.datastore().find(Service.class);

        getToggleValueQuery
                .filter("toggles.name", toggle.getName())
                .filter("name", this.name);

        if (this.version != null) {
            getToggleValueQuery.filter("version", this.version);
        }

        getToggleValueQuery.project("toggles.$", true);

        Service service = getToggleValueQuery.first();

        if (service != null) {
            for (Iterator<Toggle> toggles = service.getToggles().iterator(); toggles.hasNext(); ) {
                Toggle fetchedToggle = toggles.next();

                if (toggle.getName().equals(fetchedToggle.getName())) {
                    return fetchedToggle.getValue();
                }
            }
        }

        return null;
    }
}
