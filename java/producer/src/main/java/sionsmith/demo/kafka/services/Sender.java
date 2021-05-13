package sionsmith.demo.kafka.services;


import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
public abstract class Sender<K extends Serializable, V extends GenericRecord> implements Runnable {
    protected KafkaTemplate<K, V> template;
    protected String topicName;
    protected List<KeyValuePair<K, V>> list;
    CountDownLatch latch;



    Sender(KafkaTemplate<K, V> template, List<KeyValuePair<K, V>> list, String topicName){
        this.topicName = topicName;
        this.list = list;
        this.template = template;
        latch = new CountDownLatch(list.size());

    }


    public void sendEventTx(K key, V value) {
        template.executeInTransaction(kafkaOperations -> {
            ListenableFuture<SendResult<K, V>> f = kafkaOperations.send(topicName, key, value);
            f.addCallback(this::onSuccess, this::onError);
            return true;
        });
    }

    @Override
    public void run() {
        list.forEach(kv -> sendEventTx(kv.getKey(), kv.getValue()));
        try {
        latch.await(10, TimeUnit.SECONDS);
        log.info("Received all responses");
        } catch (InterruptedException e) {
            log.error("Timed out awaiting awaiting response!");
        }
    }

    void onSuccess(SendResult<K,V> result){
        latch.countDown();
    }

    void onError(Throwable thrown){
        latch.countDown();
    }
}
