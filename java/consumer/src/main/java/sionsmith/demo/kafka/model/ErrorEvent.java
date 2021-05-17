package sionsmith.demo.kafka.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class ErrorEvent implements Serializable {

    public static final String INPUT_RECORD_OFFSET = "input_record_offset";
    public static final String INPUT_RECORD_PARTITION = "input_record_partition";

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("partition")
    private String partition;

    @JsonProperty("offset")
    private String offset;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("CREATE_TIME")
    private String timestampType;

    @JsonProperty("key")
    private String key;

    @JsonProperty("value")
    private JsonNode value;

    @JsonProperty("headers")
    List<Header> headers;

    public String getHeaderValue(String key){
        if(!this.headers.isEmpty()) {
            for (Header header : this.headers) {
                if (header.getKey().equalsIgnoreCase(key))
                    return header.getStringValue();
            }
        }
        return null;
    }
}

@Getter
@Setter
@EqualsAndHashCode
final class Header {
    public String key;
    public String stringValue;
}

