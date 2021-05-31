package sionsmith.demo.kafka.services;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Properties;

public class KafkaPicker implements AutoCloseable {

    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(1);

    private String topicName;
    private KafkaConsumer<String, LinkedHashMap> consumer;

    public KafkaPicker(String topicName, Properties properties) {
        this.topicName = topicName;
        consumer = new KafkaConsumer<>(properties);
    }

    public ConsumerRecord<String, LinkedHashMap> pick(Long offset, Integer partition) {
        TopicPartition topicPartition = new TopicPartition(topicName, partition);
        consumer.assign(Collections.singletonList(topicPartition));
        consumer.seek(topicPartition, offset);

        ConsumerRecords<String, LinkedHashMap> records = consumer.poll(POLL_TIMEOUT);
        return records.iterator().next();
    }

    public void close() {
        consumer.close();
    }

}