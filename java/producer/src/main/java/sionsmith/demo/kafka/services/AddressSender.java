package sionsmith.demo.kafka.services;


import sionsmith.demo.address.Address;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;


@Slf4j
public class AddressSender extends Sender<String, Address> implements Runnable {

    public AddressSender(KafkaTemplate<String, Address> template, List<KeyValuePair<String, Address>> list, String topicName){
        super(template, list, topicName);
    }



    void onSuccess(SendResult<String, Address> result){
 //       log.info("SENT: " + result.toString());
        super.onSuccess(result);
    }

    void onError(Throwable thrown){
        thrown.printStackTrace();
        log.error(thrown.getLocalizedMessage());
        super.onError(thrown);
    }

}
