package sionsmith.demo.kafka;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import sionsmith.demo.kafka.config.KafkaConsumerProperties;
import sionsmith.demo.kafka.services.AddressListener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({KafkaConsumerProperties.class})
@Slf4j
public class DemoConsumerApplication implements ApplicationRunner {

    @Value("${kafka.avro.consumer.topic-name}")
    private String sourceTopic;

    private final AddressListener listener;
     DemoConsumerApplication(AddressListener listener){
         this.listener = listener;
     }

     public static void main(String[] args) {
        SpringApplication.run(DemoConsumerApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        while (true) {
            System.in.read();
            listener.seekToOffset(new TopicPartition(sourceTopic, 0), 42768);
        }
 //        final ExecutorService executorService = Executors.newFixedThreadPool(4);
//        executorService.submit(listener);
//
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            executorService.shutdown();
//            try {
//                log.info("Flushing and closing producer");
//                executorService.awaitTermination(200, TimeUnit.MILLISECONDS);
//
//            } catch (InterruptedException e) {
//                log.warn("shutting down", e);
//            }
//        }));
    }
}
