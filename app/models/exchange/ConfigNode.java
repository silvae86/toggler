package models.exchange;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import org.mongodb.morphia.annotations.*;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.libs.Json;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAlias("name")
    @Property("toggleName")
    private String toggleName;

    @Property("allowed")
    @JsonAlias("allow")
    private List<Service> allowed;

    @Reference("denied")
    @JsonAlias("deny")
    private List<Service> denied;

    @Reference("overrides")
    @JsonAlias("unless")
    private List<ConfigNode> overrides;

    @JsonProperty("value")
    @Property("value")
    private Boolean value;

    public ConfigNode() {
    }

    public void applyChanges()
    {
        // 1. Set value of all instances of this toggle
        if(value != null)
        {
            Query<Toggle> allInstancesOfToggleQuery = MongoConfig.datastore().find(Toggle.class);
            allInstancesOfToggleQuery.criteria("name").equal(toggleName);
            UpdateOperations<Toggle> setDefaultValueOperation = MongoConfig.datastore().createUpdateOperations(Toggle.class).set("value", value);
            MongoConfig.datastore().findAndModify(allInstancesOfToggleQuery, setDefaultValueOperation);
        }

        // 2. Add the toggle to all Services that have access to it
        if(allowed.size() > 0)
        {
            for (Service allowedService: allowed) {
                Toggle newAllowedToggle = new Toggle(toggleName, allowedService, value);
                allowedService.getToggles().add(newAllowedToggle);
            }
        }
        else
        {

        }

        // 3. Remove toggle from every service that is denied access
        if(denied.size() > 0)
        {

        }
        else
        {

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

            if(node.has("value"))
            {
                cn.setValue(Boolean.parseBoolean(node.get("value").textValue()));
            }

            if(node.has("toggleName"))
            {
                cn.setToggleName(node.get("toggleName").textValue());
            }
            else
            {
                ConfigNode parent = (ConfigNode) jp.getParsingContext().getParent().getCurrentValue();
                cn.setToggleName(parent.getToggleName());
            }

            if(node.has("allow"))
            {
                List<Service> allowed = Arrays.asList(mapper.readValue(node.get("allow").toString(), Service[].class));
                cn.setAllowed(allowed);
            }


            if(node.has("deny"))
            {
                List<Service> denied = mapper.readValue(node.get("deny").toString(), new TypeReference<List<Service>>() { });
                cn.setDenied(denied);
            }

            if(node.has("unless"))
            {
                List<ConfigNode> overrides = mapper.readValue(node.get("unless").toString(), new TypeReference<List<ConfigNode>>() { });
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