package sionsmith.demo.kafka.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaPicker implements AutoCloseable {

    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(1);

    private String topicName;
    private KafkaConsumer<String, JsonNode> consumer;

    public KafkaPicker(String topicName, Properties properties) {
        this.topicName = topicName;
        consumer = new KafkaConsumer<>(properties);
    }

    public JsonNode pick(Long offset, Integer partition) {
        TopicPartition topicPartition = new TopicPartition(topicName, partition);
        consumer.assign(Collections.singletonList(topicPartition));
        consumer.seek(topicPartition, offset);

        ConsumerRecords<String, JsonNode> records = consumer.poll(POLL_TIMEOUT);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(records.iterator().next().value());
    }

    public void close() {
        consumer.close();
    }

}