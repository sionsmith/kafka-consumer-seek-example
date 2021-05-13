package sionsmith.demo.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.ContainerAwareErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.serializer.DeserializationException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class KafkaErrorHandler implements ContainerAwareErrorHandler {
    @Override
    public void handle(Exception thrownException,
                       List<ConsumerRecord<?, ?>> records,
                       Consumer<?, ?> consumer,
                       MessageListenerContainer container) {

        doSeeks(records, consumer);

        if (!records.isEmpty()) {
            ConsumerRecord<?, ?> record = records.get(0);
            String topic = record.topic();
            long offset = record.offset();
            int partition = record.partition();
            if (thrownException.getClass().equals(DeserializationException.class)) {
                DeserializationException exception = (DeserializationException) thrownException;
                String malformedMessage = new String(exception.getData());
                log.info("Skipping message with topic {} and offset {} " +
                        "- malformed message: {} , exception: {}", topic, offset, malformedMessage, exception.getLocalizedMessage());
            } else {
                log.info("Skipping message with topic {} - offset {} - partition {} - exception {}", topic, offset, partition,
                        thrownException);
            }
        } else {
            log.info("Consumer exception - cause: {}", thrownException.getMessage());
        }
    }


    private void doSeeks(List<ConsumerRecord<?, ?>> records, Consumer<?, ?> consumer) {

        Map<TopicPartition, Long> partitions = new LinkedHashMap<>();
        AtomicBoolean first = new AtomicBoolean(true);

        records.forEach(record -> {
            if (first.get()) {
                partitions.put(new TopicPartition(record.topic(), record.partition()), record.offset() + 1);
            } else {
                partitions.computeIfAbsent(new TopicPartition(record.topic(), record.partition()),
                        offset -> record.offset());
            }
            first.set(false);
        });

        partitions.forEach(consumer::seek);
    }
}