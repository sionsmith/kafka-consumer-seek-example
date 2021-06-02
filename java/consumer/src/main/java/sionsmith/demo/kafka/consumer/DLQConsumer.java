package sionsmith.demo.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import sionsmith.demo.kafka.model.ErrorEvent;
import sionsmith.demo.kafka.services.ShipmentEventService;

@Component
@Slf4j
public class DLQConsumer {

    @Value("${spring.kafka.consumer.dlq-topic-name}")
    private String dlqTopic;

    @Autowired
    private ShipmentEventService shipmentEventService;

    @KafkaListener(topics = {"${spring.kafka.consumer.dlq-topic-name}"},
            autoStartup="${spring.kafka.consumer.dlq-drain-enabled}",
            groupId = "${spring.kafka.consumer.dlq-group-id}"
    )
    public void onMessage(ConsumerRecord<String, Object> consumerRecord, Acknowledgment acknowledgment) throws JsonProcessingException {
        log.info("DLQ message: {}", consumerRecord);
        try {
            String value = (String) consumerRecord.value();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(value);

            log.info("Attempted to re-process message from topic: " + this.dlqTopic);
            shipmentEventService.reProcessDlqMessage(jsonNode);
            //commit offset
            acknowledgment.acknowledge();

        } catch (JsonProcessingException e) {
            log.error("Failed parses Json message.");
        } catch (Exception e) {
            log.error("Failed re process message caused by: ", e.getMessage());
        }
    }
}