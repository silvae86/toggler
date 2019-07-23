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
    private ConfigNode override;

    @Property("all_allowed")
    private boolean allAllowed;

    @Property("all_denied")
    private boolean allDenied;

    @JsonProperty("value")
    @Property("value")
    private Boolean defaultValue;

    public ConfigNode() {
    }

    private Toggle createToggleWithValue(Toggle toggleWithDefaultValue, Service serviceWithSpecificValue) {
        Toggle newToggle = new Toggle(toggleWithDefaultValue.getName());

        if (serviceWithSpecificValue.getValue() == null) {
            newToggle.setValue(toggleWithDefaultValue.getValue());
        } else {
            newToggle.setValue(serviceWithSpecificValue.getValue());
        }

        return newToggle;
    }

    private void addToggleToServices(Toggle toggleToAdd, Iterator<Service> services) {
        while (services != null && services.hasNext()) {
            Service serviceBeingAdded = services.next();
            Service.createOrUpdateService(serviceBeingAdded);
            serviceBeingAdded.addOrUpdateToggle(createToggleWithValue(toggleToAdd, serviceBeingAdded));
        }
    }

    private void removeToggleFromServices(Toggle toggleToRemove, Iterator<Service> services) {
        while (services != null && services.hasNext()) {
            Service transientService = services.next();
            Service deniedService = Service.find(transientService);
            if (deniedService != null)
                deniedService.removeToggle(createToggleWithValue(toggleToRemove, deniedService));
            else {
                Service.createOrUpdateService(transientService);
            }
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

        if (nodeToScan.getOverride() != null) {
            allServices.addAll(override.scanForServices());
        }

        return allServices;
    }

    public void apply(ParsingContext context) throws Exception {
        System.out.println("Applying node for toggle " + context.getToggleName());
        // System.out.println(this);

        Toggle toggle = new Toggle(context.getToggleName());

        Query<Service> allServicesQuery = MongoConfig.datastore().find(Service.class);
        Iterator<Service> servicesToAddToggleTo = null;
        Iterator<Service> servicesToRemoveToggleFrom = null;

        // 1. Set defaultValue of all instances of this toggle in every service if there is a default
        if (defaultValue != null) {
            /*Query<Toggle> allInstancesOfToggleQuery = MongoConfig.datastore().find(Toggle.class);
            allInstancesOfToggleQuery.criteria("name").equal(context.getToggleName());
            UpdateOperations<Toggle> setDefaultValueOperation = MongoConfig.datastore().
                    createUpdateOperations(Toggle.class).set("defaultValue", defaultValue);
            MongoConfig.datastore().findAndModify(allInstancesOfToggleQuery, setDefaultValueOperation);*/
            context.setToggleValue(defaultValue);
            toggle.setValue(defaultValue);
        } else if (context.getToggleValue() != null) {
            toggle.setValue(context.getToggleValue());
        }

        // 2. if we have both allowed and denied services
        if (allowed != null && denied != null) {

            // 2.1 If we have only some allowed
            if (allAllowed && denied.size() > 0) {
                // 2.2 If all are allowed, then all services should have access to this toggle. Add the toggle to all services.
                servicesToAddToggleTo = allServicesQuery.iterator();
                servicesToRemoveToggleFrom = denied.iterator();

            } else if (allDenied && allowed.size() > 0) {
                // 2.1 If allowed has only some specific services, add the toggle to the services that can access it
                servicesToRemoveToggleFrom = allServicesQuery.iterator();
                servicesToAddToggleTo = allowed.iterator();
            } else if (allDenied && allAllowed) {
                throw new Exception("Cannot deny and allow all services to access toggle " + context.getToggleName());
            }
        }
        // 3. Remove toggle from every service that is denied
        else if (allowed == null) {
            if (allDenied) {
                // 3.1 If denied has only some specific services, remove the toggle from the services that cannot access it
                servicesToRemoveToggleFrom = allServicesQuery.iterator();
            } else if (denied != null && denied.size() > 0) {
                // 3.2 If denied is an empty array, no service should be allowed to access. Remove toggle from all services.
                servicesToRemoveToggleFrom = denied.iterator();
            }
        } else if (denied == null) {
            if (allAllowed) {
                servicesToAddToggleTo = allServicesQuery.iterator();
            } else if (allowed != null && allowed.size() > 0) {
                servicesToAddToggleTo = allowed.iterator();
            }
        }

        addToggleToServices(toggle, servicesToAddToggleTo);
        removeToggleFromServices(toggle, servicesToRemoveToggleFrom);

        if (this.getOverride() != null) {
            override.apply(context);
        }

        System.out.println("Applied node for toggle " + context.getToggleName());
        // System.out.println(this);
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
                cn.setDefaultValue(node.get("value").booleanValue());
            }

            if (node.has("allow")) {
                if (node.get("allow").textValue() != null && node.get("allow").textValue().equals("all")) {
                    cn.setAllAllowed(true);
                } else {
                    HashSet<Service> allowed = new HashSet<>(Arrays.asList(mapper.readValue(node.get("allow").toString(), Service[].class)));
                    cn.setAllowed(allowed);
                }
            }

            if (node.has("deny")) {
                if (node.get("deny").textValue() != null && node.get("deny").textValue().equals("all")) {
                    cn.setAllDenied(true);
                } else {
                    HashSet<Service> denied = mapper.readValue(node.get("deny").toString(), new TypeReference<HashSet<Service>>() {
                    });
                    cn.setDenied(denied);
                }
            }

            if (node.has("unless")) {
                ConfigNode override = mapper.readValue(node.get("unless").toString(), new TypeReference<ConfigNode>() {
                });
                cn.setOverride(override);
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
