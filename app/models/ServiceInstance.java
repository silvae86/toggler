package models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Getter;
import lombok.Setter;
import models.concepts.Service;
import models.concepts.Toggle;
import org.mongodb.morphia.annotations.*;
import play.libs.Json;

import java.io.IOException;
import java.util.HashSet;

@Entity("service_toggles")
@Indexes({
        @Index(fields = {@Field("toggle.name"), @Field("service.name"), @Field("service.version")}, options = @IndexOptions(unique = true, dropDups = true)),
        @Index(fields = {@Field("service.name")})
})
@Getter
@Setter
@JsonDeserialize(using = ServiceInstance.Deserializer.class)
public class ServiceInstance {

    @Reference("service")
    @JsonAlias("name")
    private Service service;

    @Reference("value")
    private boolean value;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Reference("permission_nodes")
    private HashSet<ConfigNode> toggles;

    public static class Deserializer extends StdDeserializer<ServiceInstance> {

        public Deserializer() {
            super(Toggle.class);
        }

        @Override
        public ServiceInstance deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {

            JsonNode node = jp.getCodec().readTree(jp);
            Service service = new Service();
            ServiceInstance st = new ServiceInstance();

            String name = node.get("name").textValue();
            service.setName(name);

            if (node.get("version") != null) {
                String version = node.get("version").textValue();
                service.setVersion(version);
            }

            if (node.get("value") != null) {
                boolean value = node.get("value").booleanValue();
                st.setValue(value);
            }

            st.setService(service);

            return st;
        }

        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(Json.toJson(this));
            } catch (JsonProcessingException e) {
                return ("Unable to serialize ServiceInstance: " + e.getMessage());
            }
        }
    }
}
