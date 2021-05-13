package sionsmith.demo.kafka.services;

import java.io.Serializable;
import lombok.Value;
import org.apache.avro.generic.GenericRecord;

@Value
public class KeyValuePair<K extends Serializable, V extends GenericRecord>{
    K key;
    V value;

    private KeyValuePair(K key, V value){
        this.key = key;
        this.value = value;
    }

    public static <K extends Serializable ,V extends GenericRecord> KeyValuePair<K, V> pair(K key, V value){
        return  new KeyValuePair<K,V>(key, value);
    }

}
