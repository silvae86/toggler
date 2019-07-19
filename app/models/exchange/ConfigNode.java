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
import lombok.Getter;
import lombok.Setter;
import models.database.Service;
import models.database.Toggle;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
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

    public ConfigNode() {
    }

    public void apply(String toggleName)
    {
        System.out.println("Applying node for toggle " + toggleName);
        System.out.println(this);

        Query<Service> allServicesQuery = MongoConfig.datastore().find(Service.class);
        Iterator<Service> it;

        // 1. Set value of all instances of this toggle
        if(value != null)
        {
            Query<Toggle> allInstancesOfToggleQuery = MongoConfig.datastore().find(Toggle.class);
            allInstancesOfToggleQuery.criteria("name").equal(toggleName);
            UpdateOperations<Toggle> setDefaultValueOperation = MongoConfig.datastore().createUpdateOperations(Toggle.class).set("value", value);
            MongoConfig.datastore().findAndModify(allInstancesOfToggleQuery, setDefaultValueOperation);
        }

        // 2. Add the toggle to all Services that have access to it
        if (allowed != null && allowed.size() > 0) {
            // 2.1 If allowed has only some specific services, add the toggle to the services that can access it
            it = allowed.iterator();
        } else
        {
            // 2.2 If allowed is an empty array, then all services should have access to this toggle. Add the toggle to all services.
            it = allServicesQuery.iterator();
        }

        while (it.hasNext()) {
            Service allowedService = it.next();
            Toggle newAllowedToggle = new Toggle(toggleName, allowedService, value);
            allowedService.addOrUpdateToggle(newAllowedToggle);
        }

        // 3. Remove toggle from every service that is denied
        if (denied != null && denied.size() > 0) {
            // 3.1 If denied has only some specific services, remove the toggle from the services that cannot access it
            it = denied.iterator();
        }
        else {
            // 3.2 If denied is an empty array, no service should be allowed to access. Remove toggle from all services.
            it = allServicesQuery.iterator();
        }

        while (it.hasNext()) {
            Service deniedService = it.next();
            Toggle newDeniedToggle = new Toggle(toggleName, deniedService, value);
            deniedService.removeToggle(newDeniedToggle);
        }

        if (this.getOverrides() != null) {
            for (ConfigNode override : this.getOverrides()) {
                override.apply(toggleName);
            }
        }

        System.out.println("Applied node for toggle " + toggleName);
        System.out.println(this);
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

            if(node.has("value"))
            {
                cn.setValue(Boolean.parseBoolean(node.get("value").textValue()));
            }

            if(node.has("allow"))
            {
                HashSet<Service> allowed = new HashSet<>(Arrays.asList(mapper.readValue(node.get("allow").toString(), Service[].class)));
                cn.setAllowed(allowed);
            }


            if(node.has("deny"))
            {
                HashSet<Service> denied = mapper.readValue(node.get("deny").toString(), new TypeReference<HashSet<Service>>() {
                });
                cn.setDenied(denied);
            }

            if(node.has("unless"))
            {
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