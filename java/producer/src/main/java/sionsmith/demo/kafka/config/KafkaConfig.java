package sionsmith.demo.kafka.config;

import sionsmith.demo.address.Address;
import java.util.HashMap;
import java.util.Map;
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
    @Value("${kafka.avro.producer.topic-name}")
    String topicName;

    public KafkaConfig(KafkaProperties kafkaProperties, KafkaProducerProperties kafkaProducerProperties) {
        this.kafkaProperties = kafkaProperties;
        this.kafkaProducerProperties = kafkaProducerProperties;
    }

    // In the Event Platform the topics are created by the Event Platform
    // This is just for creating locally in docker
    @Bean
    public NewTopic newTopic() {
        return new NewTopic(topicName, 1, (short) 1);
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());

        // set non-standard Kafka properties explicitly
        props.put("schema.registry.url", kafkaProducerProperties.getSchemaRegistryUrl());
        if (kafkaProducerProperties.getKeySubjectNameStrategy() != null) {
            props.put("key.subject.name.strategy", kafkaProducerProperties.getKeySubjectNameStrategy());
        }
        props.put("value.subject.name.strategy", kafkaProducerProperties.getValueSubjectNameStrategy());

        props.put("auto.register.schemas", kafkaProducerProperties.getAutoRegisterSchemas());
        props.put("use.latest.version", kafkaProducerProperties.getUseLatestVersion());

        return props;
    }


    @Bean
    public ProducerFactory<?, ?> producerFactory() {
        var factory = new DefaultKafkaProducerFactory<>(producerConfigs());
        factory.setTransactionIdPrefix(kafkaProducerProperties.getTransactionIdPrefix());
        return factory;
    }
    @Bean
    public KafkaTemplate<String, Address> addressTemplate(ProducerFactory<String, Address> pf) {
        var template = new KafkaTemplate<>(pf);
        // offers some basic error logging without calling context.  If you need to have the context then consider
        // adding a callback on the ListenableFuture  of the send method of KafkaTemplate
        template.setProducerListener(new LoggingProducerListener<>());
        return template;
    }
}
