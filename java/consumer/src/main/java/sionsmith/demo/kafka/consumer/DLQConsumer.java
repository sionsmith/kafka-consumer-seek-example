package sionsmith.demo.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import sionsmith.demo.kafka.services.ShipmentEventService;

import java.util.LinkedHashMap;

@Component
@Slf4j
public class DLQConsumer {

    @Value("${spring.kafka.consumer.dlq-topic-name}")
    private String dlqTopic;

    @Autowired
    private ShipmentEventService shipmentEventService;

    @KafkaListener(topics = {"${spring.kafka.consumer.dlq-topic-name}"},
            autoStartup="${spring.kafka.consumer.dlq-drain-enabled}",
            groupId = "${spring.kafka.consumer.dlq-consumer-group-id}"
    )
    public void onMessage(ConsumerRecord<String, LinkedHashMap> consumerRecord, Acknowledgment acknowledgment) throws Exception {
        log.info("DLQ message: {}", consumerRecord);
        try {
            log.info("Attempted to re-process message from topic: " + this.dlqTopic);
            shipmentEventService.invokeShipmentProcessingLambda(consumerRecord);
            //commit offset
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed re process DLQ message.", e);
            throw e;
        }
    }
}