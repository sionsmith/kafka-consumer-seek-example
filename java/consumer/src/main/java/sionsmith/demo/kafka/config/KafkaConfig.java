package sionsmith.demo.kafka.config;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configure the Kafka consumer client.
 */
@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {
    private final KafkaProperties kafkaProperties;
    private final KafkaConsumerProperties kafkaConsumerProperties;

    public KafkaConfig(KafkaProperties kafkaProperties, KafkaConsumerProperties kafkaConsumerProperties) {
        this.kafkaProperties = kafkaProperties;
        this.kafkaConsumerProperties = kafkaConsumerProperties;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put("security.protocol", kafkaConsumerProperties.getSecurityProtocol());
        props.put("sasl.jaas.config", kafkaConsumerProperties.getSaslJaasConfig());
        props.put("sasl.mechanism", kafkaConsumerProperties.getSaslMechanism());

        return props;
    }

    @Bean
    public ConsumerFactory<String, JsonNode> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, JsonNode> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, JsonNode> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setErrorHandler(new KafkaErrorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setSyncCommits(true);
        return factory;
    }


}
