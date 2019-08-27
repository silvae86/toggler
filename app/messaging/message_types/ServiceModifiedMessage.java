package messaging.message_types;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import models.database.Service;

public class ServiceModifiedMessage extends Message{

    @Getter
    @Setter
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
