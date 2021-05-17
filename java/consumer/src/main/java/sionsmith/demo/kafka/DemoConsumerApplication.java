package sionsmith.demo.kafka;

import sionsmith.demo.kafka.config.KafkaConsumerProperties;

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

    public static void main(String[] args) {
        SpringApplication.run(DemoConsumerApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {


    }

}
