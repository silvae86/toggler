package messaging.message_types;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import play.libs.Json;

import java.util.Map;

public abstract class Message implements IMessage{
    @JsonAlias("message_type")
    String type = getMessageType();
}
