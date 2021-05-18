package sionsmith.demo.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import sionsmith.demo.kafka.model.ErrorEvent;
import sionsmith.demo.kafka.services.ShipmentEventService;

@Component
@Slf4j
public class ErrorEventConsumer {

    @Autowired
    private ShipmentEventService shipmentEventService;

    @KafkaListener(topics = {"${kafka.consumer.error-topic-name}"})
    public void onMessage(ConsumerRecord<String, Object> consumerRecord) throws JsonProcessingException {
        log.info("Sink retry message: {}", consumerRecord);
        try {
            String value = (String) consumerRecord.value();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNodes = objectMapper.readTree(value);
            if (jsonNodes.isArray()) {
                for (JsonNode jsonNode : jsonNodes) {
                    ErrorEvent errorEvent = objectMapper.treeToValue(jsonNode.get("payload"), ErrorEvent.class);
                    Long offset = Long.parseLong(errorEvent.getHeaderValue(ErrorEvent.INPUT_RECORD_OFFSET));
                    Integer partition = Integer.parseInt(errorEvent.getHeaderValue(ErrorEvent.INPUT_RECORD_PARTITION));
                    log.info("Attempted to re-process message offset: " + offset + " on partition: " + partition);
                    shipmentEventService.reProcessFailedEvent(offset, partition);
                }
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}