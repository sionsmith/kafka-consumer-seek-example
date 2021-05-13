package sionsmith.demo.kafka;

import static java.util.stream.Collectors.toList;
import sionsmith.demo.address.Address;
import sionsmith.demo.kafka.config.KafkaProducerProperties;
import sionsmith.demo.kafka.services.AddressSender;
import sionsmith.demo.kafka.services.KeyValuePair;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import sionsmith.demo.kafka.services.metrics.AddressMetricsReporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({KafkaProducerProperties.class})
public class DemoProducerApplication implements ApplicationRunner {
    @Value("${kafka.avro.producer.topic-name}")
    private String topicName;

    final KafkaTemplate<String, Address> addressTemplate;
    final AddressMetricsReporter addressMetricsReporter;

    public DemoProducerApplication(KafkaTemplate<String, Address> addressTemplate,
                                   AddressMetricsReporter addressMetricsReporter) {
        this.addressTemplate = addressTemplate;
        this.addressMetricsReporter = addressMetricsReporter;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoProducerApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        AddressSender addressSender = new AddressSender(addressTemplate, createAddressList(250000), topicName);

        final ExecutorService executorService = Executors.newFixedThreadPool(4);

        executorService.submit(addressSender);
        executorService.submit(addressMetricsReporter);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();
            try {
                log.info("Flushing and closing producer");
                executorService.awaitTermination(200, TimeUnit.MILLISECONDS);

            } catch (InterruptedException e) {
                log.warn("shutting down", e);
            }
        }));

    }

    List<KeyValuePair<String, Address>> createAddressList(int size){
            return IntStream.rangeClosed(1, size).mapToObj(i -> KeyValuePair.pair("Address-" + String.valueOf(i), Address.newBuilder()
                .setEventID(String.valueOf(i))
                .setCorrelationID(String.valueOf(i))
                .setPayloadURI("localhost/payload")
                .setEventDateTime(i)
                .setPublishedDateTime(System.currentTimeMillis())
                .setSubjectIdentifier("ADD-1")
                .build())).collect(toList());
        }
}
