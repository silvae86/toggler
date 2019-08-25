package messaging.serializers;

import messaging.message_types.Message;
import org.apache.kafka.common.serialization.Serializer;
import play.libs.Json;

import java.util.Map;

public class JsonMessageSerializer implements Serializer<Message> {
    public JsonMessageSerializer(){};
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }
    @Override
    public byte[] serialize(String topic, Message data) {
        byte[] retVal = null;
        try {
            retVal = Json.toJson(data).textValue().getBytes();
        } catch (Exception exception) {
            System.out.println("Error in serializing object"+ data);
        }
        return retVal;
    }
    @Override
    public void close() {
    }
}
