package sionsmith.demo.kafka.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Properties;

@Slf4j
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
        try {
            return records.iterator().next();
        } catch (NoSuchElementException e) {
            log.error(String.format("There is no message with offset: %d, partition: %d in the source topic.",
                    offset, partition), e);
            throw e;
        }
    }

    public void close() {
        consumer.close();
    }

}