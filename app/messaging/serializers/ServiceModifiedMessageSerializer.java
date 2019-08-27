package messaging.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import messaging.message_types.ServiceModifiedMessage;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class ServiceModifiedMessageSerializer implements Serializer<ServiceModifiedMessage> {
    public ServiceModifiedMessageSerializer() {
    }

    ;
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }
    @Override
    public byte[] serialize(String topic, ServiceModifiedMessage data) {
        byte[] retVal = null;
        try {
            retVal = new ObjectMapper().writeValueAsBytes(data);
        } catch (Exception exception) {
            System.out.println("Error in serializing object"+ data);
        }
        return retVal;
    }
    @Override
    public void close() {
    }
}
