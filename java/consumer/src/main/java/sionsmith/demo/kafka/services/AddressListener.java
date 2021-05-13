package sionsmith.demo.kafka.services;


import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AbstractConsumerSeekAware;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import sionsmith.demo.address.Address;

import java.util.Map;

@Component
public class AddressListener extends AbstractConsumerSeekAware {

    @KafkaListener(id = "so634292011", topics = "${kafka.avro.consumer.topic-name}", concurrency = "2")
    public void listen(@Payload Address record) {
        System.out.println("@@@@" + record.getCorrelationID());
    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
//        System.out.println(assignments);
        super.onPartitionsAssigned(assignments, callback);
        callback.seekToBeginning(assignments.keySet());
    }

    public void seekToTime(long time) {
        getSeekCallbacks().forEach((tp, callback) -> callback.seekToTimestamp(tp.topic(), tp.partition(), time));
    }

    public void seekToOffset(TopicPartition tp, long offset) {
        getSeekCallbackFor(tp).seek(tp.topic(), tp.partition(), offset);
    }

}