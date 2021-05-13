//package sionsmith.demo.kafka.services;
//
//import sionsmith.demo.address.Address;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.avro.specific.SpecificRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.kafka.annotation.KafkaHandler;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//@KafkaListener(topics = "${kafka.avro.consumer.topic-name}",
//        containerFactory = "kafkaListenerContainerFactory",
//        groupId = "${kafka.avro.consumer.group-id}")
//public class ConsumerService {
//
//    /**
//     * Handler for Address records.
//     *
//     * Implement business logic here related to processing mesages of type Address.
//     *
//     * @param record deserialized message
//     * @param meta message Kafka metadata
//     */
//    @KafkaHandler
//    public void listen(@Payload Address record, ConsumerRecordMetadata meta) {
//        log.info(record.toString());
//    }
//
//
//    /**
//     * Fallback message handler.
//     * Invoked when there isn't a method handler for a specific type.
//     * This handler is invoked when the Avro deserialization was successful
//     * but there is no codepath to handling the received object type.
//     *
//     * @param record deserialized message
//     * @param meta message Kafka metadata
//     */
//    @KafkaHandler(isDefault = true)
//    public void listenDefault(@Payload SpecificRecord object, ConsumerRecordMetadata meta) {
// //       log.info(object.toString());
//    }
//
//
//}
