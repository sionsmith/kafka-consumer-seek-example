package sionsmith.demo.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.producer")
@Getter
@Setter
public class KafkaProducerProperties {
    private Class<?> keySubjectNameStrategy;
    private Class<?> valueSubjectNameStrategy;
}
