package sionsmith.demo.kafka.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sionsmith.demo.kafka.config.KafkaConsumerProperties;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Properties;

@Service
@Slf4j
public class ShipmentEventService {
    @Value("${lambda.region}")
    private String awsRegion;

    @Value("${lambda.function-name}")
    private String lambdaFunctionName;

    private LambdaClient client;

    @Autowired
    private KafkaConsumerProperties kafkaConsumerProperties;

    private Properties shipmentTopicProperties;

    @PostConstruct
    private void init() {
        client = LambdaClient.builder()
                .region(Region.of(awsRegion))
                .build();
        shipmentTopicProperties = new Properties();
        shipmentTopicProperties.put("bootstrap.servers", kafkaConsumerProperties.getBootstrapServers());
        shipmentTopicProperties.put("group.id", kafkaConsumerProperties.getSourceTopicPickerConsumerGroupId());
        shipmentTopicProperties.put("key.deserializer", io.confluent.kafka.serializers.KafkaJsonDeserializer.class);
        shipmentTopicProperties.put("value.deserializer", io.confluent.kafka.serializers.KafkaJsonDeserializer.class);
        shipmentTopicProperties.put("spring.json.trusted.packages", kafkaConsumerProperties.getSpringJsonTrustedPackages());
        shipmentTopicProperties.put("max.poll.records", kafkaConsumerProperties.getMaxPollRecords());
        shipmentTopicProperties.put("security.protocol", kafkaConsumerProperties.getSecurityProtocol());
        shipmentTopicProperties.put("sasl.jaas.config", kafkaConsumerProperties.getSaslJaasConfig());
        shipmentTopicProperties.put("sasl.mechanism", kafkaConsumerProperties.getSaslMechanism());


    }

    public void reProcessFailedEvent(String sourceTopic, Long offset, Integer partition) throws Exception {
        try (KafkaPicker kafkaPicker = new KafkaPicker(sourceTopic, shipmentTopicProperties)) {
            ConsumerRecord<String, LinkedHashMap> record = kafkaPicker.pick(offset, partition);
            log.info(String.format("Retrieved payload from offset: %d, partition: %d, key: %s Payload: %s",
                    offset,
                    partition,
                    record.key(),
                    record.value().toString()));

            invokeShipmentProcessingLambda(record);
        } catch (Exception e) {
            log.error(String.format("Failed to process message from offset: %d, partition: %d ", offset, partition));
            throw e;
        }
    }

    public void invokeShipmentProcessingLambda(ConsumerRecord<String, LinkedHashMap> record) throws Exception {
        for (int retries = 0; ; retries++) {
            try {
                //build payload object as Lambda expects (same as sink connector)
                String payload = buildPayloadArray(record);
                //Invoke the Lambda function
                InvokeResponse response = client.invoke(InvokeRequest.builder()
                        .functionName(lambdaFunctionName)
                        .invocationType("RequestResponse")
                        .payload(SdkBytes.fromUtf8String(payload))
                        .build());

                log.info("Lambda was invoked. Response: " + response.statusCode());
                if (response.functionError() != null || response.statusCode() != 200) {
                    String errorMsg = String.format(
                            "Lambda invocation returned error. Message: %s",
                            response.payload().asString(StandardCharsets.UTF_8));
                    throw new Exception(errorMsg);
                }
                break;
            } catch (Exception e) {
                if (retries < 3) {
                    log.warn(String.format("Lambda failed with an error: %s. Another attempt will be made.", e.getMessage()));
                    continue; // try calling the lambda again
                } else {
                    log.error("After retries Lambda failed to process payload from offset.");
                    throw e;
                }
            }
        }
    }

    private String buildPayloadArray(ConsumerRecord<String, LinkedHashMap> record) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode rootNode = mapper.createObjectNode();

        ObjectNode childNode1 = mapper.createObjectNode();
        childNode1.put("timestamp", record.timestamp());
        childNode1.put("topic", record.topic());
        childNode1.put("partition", record.partition());
        childNode1.put("offset", record.offset());
        childNode1.put("key", String.valueOf(record.key()));
        childNode1.put("value", mapper.valueToTree(record.value()));
        rootNode.set("payload", childNode1);

        ArrayNode arr = mapper.createArrayNode();
        arr.add(rootNode);
        return mapper.writeValueAsString(arr);
    }
}
