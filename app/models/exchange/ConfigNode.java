package models.exchange;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import database.MongoConfig;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import lombok.Getter;
import lombok.Setter;
import models.database.Service;
import models.database.Toggle;
import org.bson.types.ObjectId;
import play.libs.Json;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

//@Indexes({
//        @Index(fields = {@Field("toggle.toggleName"), @Field("service.toggleName"), @Field("service.version")}, options = @IndexOptions(unique = true, dropDups = true)),
//        @Index(fields = {@Field("service.toggleName")})
//})

@Getter
@Setter
@JsonDeserialize(using = ConfigNode.Deserializer.class)
public class ConfigNode {
    @Id
    private ObjectId id;

    @Embedded("allowed")
    @JsonAlias("allow")
    private HashSet<Service> allowed;

    @Embedded("denied")
    @JsonAlias("deny")
    private HashSet<Service> denied;

    @Embedded("overrides")
    @JsonAlias("unless")
    private HashSet<ConfigNode> overrides;

    @JsonProperty("value")
    @Property("value")
    private Boolean value;

    private Config ownerConfig;

    private String toggleName;

    public ConfigNode(Config ownerConfig, String toggleName) {
        this.ownerConfig = ownerConfig;
        this.toggleName = toggleName;
    }

    public ConfigNode() {
    }

    private void addToggleToServices(Toggle toggleToAdd, Iterator<Service> services) {
        while (services.hasNext()) {
            Service allowedService = Service.find(services.next());
            MongoConfig.datastore().save(allowedService);
            Toggle newAllowedToggle = new Toggle(toggleToAdd.getName(), (toggleToAdd.getValue() == null) ? value : toggleToAdd.getValue());
            allowedService.addOrUpdateToggle(newAllowedToggle);
        }
    }

    private void removeToggleFromServices(Toggle toggleToRemove, Iterator<Service> services) {
        while (services.hasNext()) {
            Service deniedService = Service.find(services.next());
            MongoConfig.datastore().save(deniedService);
            Toggle newDeniedToggle = new Toggle(toggleToRemove.getName(), (toggleToRemove.getValue() == null) ? value : toggleToRemove.getValue());
            deniedService.removeToggle(newDeniedToggle);
        }
    }

    public HashSet<Service> scanForServices() {
        return scanForServices(this);
    }

    private HashSet<Service> scanForServices(ConfigNode nodeToScan) {
        HashSet<Service> allServices = new HashSet<>();

        if (nodeToScan.allowed != null)
            allServices.addAll(nodeToScan.allowed);

        if (nodeToScan.denied != null)
            allServices.addAll(nodeToScan.denied);

        if (nodeToScan.getOverrides() != null) {
            for (ConfigNode override : nodeToScan.getOverrides()) {
                allServices.addAll(override.scanForServices());
            }
        }

        return allServices;
    }

    public void apply() {
        System.out.println("Applying node for toggle " + toggleName);
        System.out.println(this);

        Query<Service> allServicesQuery = MongoConfig.datastore().find(Service.class);
        Iterator<Service> servicesToAddToggleTo = null;
        Iterator<Service> servicesToRemoveToggleFrom = null;

        // 1. Set value of all instances of this toggle
        if (value != null) {
            Query<Toggle> allInstancesOfToggleQuery = MongoConfig.datastore().find(Toggle.class);
            allInstancesOfToggleQuery.criteria("name").equal(toggleName);
            UpdateOperations<Toggle> setDefaultValueOperation = MongoConfig.datastore().
                    createUpdateOperations(Toggle.class).set("value", value);
            MongoConfig.datastore().findAndModify(allInstancesOfToggleQuery, setDefaultValueOperation);
        }

        // 2. Add the toggle to all Services that have access to it
        if (allowed != null && denied != null) {
            if (allowed.size() == 0 && denied.size() > 0) {
                // 2.2 If allowed is an empty array, then all services should have access to this toggle. Add the toggle to all services.
                servicesToAddToggleTo = allServicesQuery.iterator();
                servicesToRemoveToggleFrom = denied.iterator();

            } else if (allowed.size() > 0 && denied.size() == 0) {
                {
                    // 2.1 If allowed has only some specific services, add the toggle to the services that can access it
                    servicesToRemoveToggleFrom = allServicesQuery.iterator();
                    servicesToAddToggleTo = allowed.iterator();
                }
            } else if (allowed == null) {
                // 3. Remove toggle from every service that is denied
                if (denied.size() == 0) {
                    // 3.1 If denied has only some specific services, remove the toggle from the services that cannot access it
                    servicesToRemoveToggleFrom = allServicesQuery.iterator();
                } else {
                    // 3.2 If denied is an empty array, no service should be allowed to access. Remove toggle from all services.
                    servicesToRemoveToggleFrom = denied.iterator();
                }
            } else if (denied == null) {
                if (allowed.size() == 0) {
                    servicesToAddToggleTo = allServicesQuery.iterator();
                } else {
                    servicesToAddToggleTo = allowed.iterator();
                }
            }

//            addToggleToServices(servicesToAddToggleTo);
//            removeToggleFromServices(servicesToRemoveToggleFrom);

            if (this.getOverrides() != null) {
                for (ConfigNode override : this.getOverrides()) {
                    // override.apply(toggleName);
                }
            }

            System.out.println("Applied node for toggle " + toggleName);
            System.out.println(this);
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

    public static class Deserializer extends StdDeserializer<ConfigNode> {
        public Deserializer() {
            super(ConfigNode.class);
        }

        @Override
        public ConfigNode deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = jp.getCodec().readTree(jp);

            ConfigNode cn = new ConfigNode();

            if (node.has("value")) {
                cn.setValue(Boolean.parseBoolean(node.get("value").textValue()));
            }

            if (node.has("allow")) {
                HashSet<Service> allowed = new HashSet<>(Arrays.asList(mapper.readValue(node.get("allow").toString(), Service[].class)));
                cn.setAllowed(allowed);
            }


            if (node.has("deny")) {
                HashSet<Service> denied = mapper.readValue(node.get("deny").toString(), new TypeReference<HashSet<Service>>() {
                });
                cn.setDenied(denied);
            }

            if (node.has("unless")) {
                HashSet<ConfigNode> overrides = mapper.readValue(node.get("unless").toString(), new TypeReference<HashSet<ConfigNode>>() {
                });
                cn.setOverrides(overrides);
            }


            return cn;
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
}