package messaging.message_types;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.apache.kafka.common.serialization.Serializer;
import play.libs.Json;

import java.util.Map;

public interface IMessage {
    String getMessageType();
}
