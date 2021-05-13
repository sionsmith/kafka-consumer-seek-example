package sionsmith.demo.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kafka consumer config parameters
 */
@ConfigurationProperties(prefix = "kafka.avro.consumer")
@Getter
@Setter
public class KafkaConsumerProperties {
    private String schemaRegistryUrl;
    private Boolean specificReader = false;
    private String topicName;
    private String groupId;
    private Class<?> keySubjectNameStrategy;
    private Class<?> valueSubjectNameStrategy;
}
