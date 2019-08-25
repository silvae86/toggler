package messaging.message_types;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.database.Service;
import org.apache.kafka.common.serialization.Serializer;
import play.libs.Json;

import java.util.Map;

public class ServiceModifiedMessage extends Message{

    @JsonAlias("new_service_config")
    Service service;

    @Override
    public String getMessageType() {
        return "service_modified";
    }

    public ServiceModifiedMessage(Service service) {
        this.service = service;
    }


}
