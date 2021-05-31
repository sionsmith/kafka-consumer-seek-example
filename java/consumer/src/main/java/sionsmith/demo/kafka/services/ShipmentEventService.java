package sionsmith.demo.kafka.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sionsmith.demo.kafka.config.KafkaConsumerProperties;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

import javax.annotation.PostConstruct;
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
        shipmentTopicProperties.put("group.id", kafkaConsumerProperties.getRetryConsumerGroupId());
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
            log.info("Retrived payload from offset: " + " Payload: " + record.value().toString());
            for (int retries = 0; ; retries++) {
                try {
                    //build payload object as Lambda expects (same as sink connector)
                    JSONArray payload = buildPayloadArray(record);
                    //Invoke the Lambda function
                    InvokeResponse response = client.invoke(InvokeRequest.builder()
                            .functionName(lambdaFunctionName)
                            .invocationType("RequestResponse")
                            .payload(SdkBytes.fromUtf8String(payload.toString()))
                            .build());

                    log.debug("Lambda Response: " + response.statusCode());
                    break;
                } catch (LambdaException e) {
                    if (retries < 3) {
                        continue; // try calling the lambda again
                    } else {
                        log.error("Failed to process payload from offset: " + offset + " partition: " + partition + "caused by:", e);
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to read message from offset: " + offset + " partition: " + partition + " Caused by: ", e);
            throw e;
        }
    }

    private JSONArray buildPayloadArray(ConsumerRecord<String, LinkedHashMap> record) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        ObjectNode childNode1 = mapper.createObjectNode();
        childNode1.put("timestamp", record.timestamp());
        childNode1.put("topic", record.topic());
        childNode1.put("partition", record.partition());
        childNode1.put("offset", record.offset());
        childNode1.put("key", record.key());
        childNode1.put("value", mapper.valueToTree(record.value()));
        rootNode.set("payload", childNode1);

        return new JSONArray().put(rootNode);
    }
}
