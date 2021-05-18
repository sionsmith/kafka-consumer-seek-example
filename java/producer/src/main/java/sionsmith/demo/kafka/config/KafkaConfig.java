package sionsmith.demo.kafka.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.LoggingProducerListener;


@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;
    private final KafkaProducerProperties kafkaProducerProperties;
    @Value("${kafka.producer.topic-name}")
    String topicName;

    public KafkaConfig(KafkaProperties kafkaProperties, KafkaProducerProperties kafkaProducerProperties) {
        this.kafkaProperties = kafkaProperties;
        this.kafkaProducerProperties = kafkaProducerProperties;
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());

        // set non-standard Kafka properties explicitly

        return props;
    }

    @Bean
    public ProducerFactory<String, JsonNode> producerFactory() {
        return new DefaultKafkaProducerFactory<String, JsonNode>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, JsonNode> kafkaTemplate()
    {
        return new KafkaTemplate<String, JsonNode>(producerFactory());
    }
}
