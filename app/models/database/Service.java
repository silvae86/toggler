package models.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Transient
    private Boolean value;

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
            return null;
    }

    public static Service findByNameAndVersion(String name, String version) {
        Query<Service> query = MongoConfig.datastore().find(Service.class);
        query.and(
                query.criteria("name").equal(name),
                query.criteria("version").equal(version)
        );

        return query.get();
    }

    public static Service findByName(String name) {
        Query<Service> query = MongoConfig.datastore().find(Service.class);
        query.criteria("name").equal(name);
        return query.first();
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

    public static Service createOrUpdateService(Service transientService) {
        Service existingService = find(transientService);

        if (existingService == null) {
            MongoConfig.datastore().save(transientService);
            return find(transientService);
        } else {
            existingService.setValue(transientService.value);
            MongoConfig.datastore().save(existingService);
            return existingService;
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

            createUpdateOperations(Service.class)
            .disableValidation()
            .set("toggles.$.value", toggle.getValue())
            .enableValidation();

            MongoConfig.datastore().update(serviceWithNameAndToggleQuery, setToggleValueOperation);
        } else {
            final UpdateOperations<Service> toggleInsertOperation = MongoConfig.datastore()
                    .createUpdateOperations(Service.class)
                    .push("toggles", toggle);

            MongoConfig.datastore().findAndModify(serviceWithNameAndToggleQuery, toggleInsertOperation);
        }
    }

    public boolean canAccess(Toggle t) {
        if (this.version != null) {
            Service existingService = Service.findByNameAndVersion(this.name, this.version);
            if (existingService != null)
                return toggleExistsForServiceNameAndVersion(t);
            else
                return toggleExistsForServiceName(t);
        } else {
            return toggleExistsForServiceName(t);
        }
    }

    private boolean toggleExists(Toggle t) {
        return toggleExistsForServiceNameAndVersion(t);
    }

    public Boolean getToggleValue(Toggle t) {
        Toggle fetchedToggle = fetchToggleForServiceNameAndVersion(t);

        if (fetchedToggle != null) {
            return fetchedToggle.getValue();
        } else {
            fetchedToggle = fetchToggleForServiceName(t);

            if (fetchedToggle != null) {
                return fetchedToggle.getValue();
            } else {
                return null;
            }
        }
    }

    private Toggle fetchToggleFromService(Service s, Toggle t) {
        Iterator<Toggle> serviceToggles = s.getToggles().iterator();
        while (serviceToggles.hasNext()) {
            Toggle fetchedToggle = serviceToggles.next();

            if (t.getName().equals(fetchedToggle.getName())) {
                return fetchedToggle;
            }
        }

        return null;
    }

    private Toggle fetchToggleForServiceName(Toggle toggle) {
        Query<Service> serviceWithNameAndToggleQuery = MongoConfig.datastore().find(Service.class);
        serviceWithNameAndToggleQuery
                .criteria("toggles.name").equal(toggle.getName())
                .criteria("name").equal(this.name);

        Service service = serviceWithNameAndToggleQuery.first();

        if (service != null) {
            return fetchToggleFromService(service, toggle);
        }

        return null;
    }

    private Toggle fetchToggleForServiceNameAndVersion(Toggle toggle) {
        Query<Service> serviceWithNameAndToggleQuery = MongoConfig.datastore().find(Service.class);
        serviceWithNameAndToggleQuery
                .criteria("toggles.name").equal(toggle.getName())
                .criteria("name").equal(this.name);

        if (this.version != null) {
            serviceWithNameAndToggleQuery.criteria("version").equal(this.version);
        }

        Service service = serviceWithNameAndToggleQuery.first();

        if (service != null) {
            return fetchToggleFromService(service, toggle);
        }

        return null;
    }

    private boolean toggleExistsForServiceName(Toggle toggle) {
        Toggle t = fetchToggleForServiceName(toggle);
        return (t != null);
    }

    private boolean toggleExistsForServiceNameAndVersion(Toggle toggle) {
        Toggle t = fetchToggleForServiceNameAndVersion(toggle);
        return (t != null);
    }
}
