package models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Getter;
import lombok.Setter;
import models.concepts.Service;
import org.mongodb.morphia.annotations.*;

import java.io.IOException;

@Entity("service_toggles")
@Indexes({
        @Index(fields = {@Field("toggle.name"), @Field("service.name"), @Field("service.version")}, options = @IndexOptions(unique = true, dropDups = true)),
        @Index(fields = {@Field("service.name")})
})
@Getter
@Setter
@JsonDeserialize(using = ServiceToggle.Deserializer.class)
public class ServiceToggle {

    @JsonAlias("name")
    private Service appliesTo;
    @Reference("value")
    private boolean value;

    public static class Deserializer extends StdDeserializer<ServiceToggle> {

        public Deserializer() {
            super(ServiceToggle.class);
        }

        @Override
        public ServiceToggle deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            JsonNode node = jp.getCodec().readTree(jp);
            Service service = new Service();
            ServiceToggle st = new ServiceToggle();

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

            st.setAppliesTo(service);

            return st;
        }
    }
}
