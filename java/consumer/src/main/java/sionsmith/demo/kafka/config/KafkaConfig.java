package sionsmith.demo.kafka.config;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configure the Kafka consumer client.
 */
@Configuration
@EnableKafka
public class KafkaConfig {
    private final KafkaProperties kafkaProperties;
    private final KafkaConsumerProperties kafkaConsumerProperties;
    @Value("${kafka.avro.consumer.topic-name}")
    String topicName;

    public KafkaConfig(KafkaProperties kafkaProperties, KafkaConsumerProperties kafkaConsumerProperties) {
        this.kafkaProperties = kafkaProperties;
        this.kafkaConsumerProperties = kafkaConsumerProperties;
    }

    @Bean
    public NewTopic adviceTopic() {
        return new NewTopic(topicName, 1, (short) 1);
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());

        // set non-standard Kafka properties explicitly
        props.put("schema.registry.url", kafkaConsumerProperties.getSchemaRegistryUrl());
        props.put("specific.avro.reader", kafkaConsumerProperties.getSpecificReader());

        if (kafkaConsumerProperties.getKeySubjectNameStrategy() != null) {
            props.put("key.subject.name.strategy", kafkaConsumerProperties.getKeySubjectNameStrategy());
        }
        props.put("value.subject.name.strategy", kafkaConsumerProperties.getValueSubjectNameStrategy());

        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, io.confluent.kafka.serializers.KafkaAvroDeserializer.class);

        return props;
    }

    @Bean
    public ConsumerFactory<GenericRecord, GenericRecord> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<GenericRecord, GenericRecord> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<GenericRecord, GenericRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setErrorHandler(new KafkaErrorHandler());
        return factory;
    }


}
