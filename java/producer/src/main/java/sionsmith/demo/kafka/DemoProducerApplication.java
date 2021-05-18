package sionsmith.demo.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import sionsmith.demo.kafka.config.KafkaProducerProperties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({KafkaProducerProperties.class})
public class DemoProducerApplication implements ApplicationRunner {

    @Value("${kafka.producer.topic-name}")
    private String topicName;

    final KafkaTemplate<String, JsonNode> kafkaTemplate;

    @Value("classpath:sample_event.json")
    private Resource sampleEvent;

    @Autowired
    public DemoProducerApplication(KafkaTemplate<String, JsonNode> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoProducerApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException, IOException {
        while (true) {
            //load sample JSON data file
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(sampleEvent.getInputStream());
            this.kafkaTemplate.send(topicName, jsonNode);
            Thread.sleep(1000L);
        }
    }
}
