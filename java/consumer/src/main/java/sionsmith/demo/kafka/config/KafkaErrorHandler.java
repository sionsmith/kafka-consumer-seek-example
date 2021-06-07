package sionsmith.demo.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.kafka.KafkaException;
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
        if (!records.isEmpty()) {
            ConsumerRecord<?, ?> record = records.get(0);
            String topic = record.topic();
            long offset = record.offset();
            int partition = record.partition();
            if (thrownException.getClass().equals(DeserializationException.class)) {
                // Move forward
                doSeeks(records, consumer);

                DeserializationException exception = (DeserializationException) thrownException;
                String malformedMessage = new String(exception.getData());
                log.info("Skipping message with topic {} and offset {} " +
                        "- malformed message: {} , exception: {}", topic, offset, malformedMessage, exception.getLocalizedMessage());
            } else {
                log.error("Consumer exception. Stopping consumption. Cause: {}", thrownException.getMessage());
                this.stopContainer(container, thrownException);
            }
        } else {
            log.error("Consumer exception. Stopping consumption. Cause: {}", thrownException.getMessage());
            this.stopContainer(container, thrownException);
        }
    }

    private void stopContainer(MessageListenerContainer container, Exception thrownException) {
        // The following is copied from org.springframework.kafka.listener.ContainerStoppingErrorHandler
        var executor = new SimpleAsyncTaskExecutor();
        executor.execute(container::stop);

        // isRunning is false before the container.stop() waits for listener thread
        int n = 0;
        while (container.isRunning() && n++ < 100) { // NOSONAR magic #
            try {
                Thread.sleep(100); // NOSONAR magic #
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new KafkaException("Stopped container", KafkaException.Level.ERROR, thrownException);
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