package sionsmith.demo.kafka.services.metrics;

import sionsmith.demo.address.Address;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AddressMetricsReporter extends ProducerMetricsReporter<Address> {
    public AddressMetricsReporter(KafkaTemplate<String, Address> template){
        super(template);
    }
}
