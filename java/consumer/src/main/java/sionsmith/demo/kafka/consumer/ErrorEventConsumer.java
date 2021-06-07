package sionsmith.demo.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import sionsmith.demo.kafka.model.ErrorEvent;
import sionsmith.demo.kafka.services.ShipmentEventService;

@Component
@Slf4j
public class ErrorEventConsumer {

    @Autowired
    private ShipmentEventService shipmentEventService;

    @KafkaListener(topics = {"${spring.kafka.consumer.error-topic-name}"}, autoStartup="${spring.kafka.consumer.error-drain-enabled}")
    public void onMessage(ConsumerRecord<String, Object> consumerRecord, Acknowledgment acknowledgment) throws Exception {
        log.info("Received error topic message: {}", consumerRecord.toString());
        try {
            String value = (String) consumerRecord.value();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(value);
            //map node to Error event class
            ErrorEvent errorEvent = objectMapper.treeToValue(jsonNode, ErrorEvent.class);

            Long offset = Long.parseLong(errorEvent.getHeaderValue(ErrorEvent.INPUT_RECORD_OFFSET));
            Integer partition = Integer.parseInt(errorEvent.getHeaderValue(ErrorEvent.INPUT_RECORD_PARTITION));
            String sourceTopic = errorEvent.getHeaderValue(ErrorEvent.INPUT_RECORD_TOPIC);
            log.info("Will attempt to re-process message from topic: " + sourceTopic + " using offset: " + offset + " on partition: " + partition);
            shipmentEventService.reProcessFailedEvent(sourceTopic, offset, partition);
            //commit offset
            acknowledgment.acknowledge();

        } catch (JsonProcessingException e) {
            log.error("Failed parses Json message");
            throw e;
        } catch (Exception e) {
            log.error("Failed re process message");
            throw e;
        }
    }
}