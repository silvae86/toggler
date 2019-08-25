package constants;

import com.typesafe.config.ConfigFactory;

public interface IKafkaConstants {
    public static String KAFKA_BROKERS = ConfigFactory.load().getString("kafka.server");
    public static Integer MESSAGE_COUNT=ConfigFactory.load().getInt("kafka.message_count");
    public static String CLIENT_ID=ConfigFactory.load().getString("kafka.client_id");
    public static String TOPIC_NAME=ConfigFactory.load().getString("kafka.topic_name");
    public static String GROUP_ID_CONFIG=ConfigFactory.load().getString("kafka.group_id");
    public static Integer MAX_NO_MESSAGE_FOUND_COUNT=ConfigFactory.load().getInt("kafka.max_no_message_found");
    public static String OFFSET_RESET_LATEST=ConfigFactory.load().getString("kafka.offset_reset_latest");
    public static String OFFSET_RESET_EARLIER=ConfigFactory.load().getString("kafka.offset_reset_earlier");
    public static Integer MAX_POLL_RECORDS=ConfigFactory.load().getInt("kafka.max_poll_records");
    public static Integer TIMEOUT=ConfigFactory.load().getInt("kafka.timeout");
}