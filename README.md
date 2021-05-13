# Sample Kafka producer and consumer using Spring Framework

## How to run the sample applications

1. Start up the kafka stack by running in the `docker` directory.
     ```
    docker-compose -f ./docker/single-broker-schema-registry.yml up 
    ``` 
2. Install the Producer with Maven. The avro plugin wil create the java artifacts from the avro schemas in resources.
     ```
     mvn -f java/producer/pom.xml clean install 
     ``` 
3. Register the schemas in schema registry by running the schema-registry maven plugin.
     ```
     mvn -f java/producer/pom.xml schema-registry:register
     ``` 
5. Run the Producer
    ```
    java -jar java/producer/target/demo-producer-0.0.1-SNAPSHOT.jar
    ```
6. Install the Consumer with Maven. The avro plugin wil create the java artifacts from the avro schemas in resources.
     ```
     mvn -f java/consumer/pom.xml clean install 
     ``` 
5. Run the Consumer
    ```
    java -jar java/consumer/target/demo-consumer-0.0.1-SNAPSHOT.jar
    ```
6. Destroy the environment 
    ```shell
    docker-compose -f ./docker/single-broker-schema-registry.yml down -v
    ```

## Kafka configuration

The producer and consumer configs can be set inside `application.yaml` or overriden via environmental variables.

[Reference - how Spring Boot processes external configuration](https://docs.spring.io/spring-boot/docs/2.4.1/reference/html/spring-boot-features.html#boot-features-external-config)

Reference for all available configuration parameters of the Kafka consumer and producer:

- [Producer configs](https://kafka.apache.org/documentation/#producerconfigs)
- [Consumer configs](https://kafka.apache.org/documentation/#consumerconfigs)

Additional configuration when using the Schema Registry is set in the `KafkaConfig` class.

[Reference - Configuration Options for Schema Registry](https://docs.confluent.io/platform/current/schema-registry/connect.html#configuration-options)


## Avro Schema
Using the [avro-maven-plugin](https://mvnrepository.com/artifact/org.apache.avro/avro-maven-plugin)
we can generate Java classes based on the AVRO schema files (`resources/avro/**/*.avsc`) and reference them in our code.

## Schema Registry
A Maven plugin for Schema Registry is available to help throughout the development process.
https://docs.confluent.io/platform/current/schema-registry/develop/maven-plugin.html#sr-maven-plugin

To upload and register the local schemas to a Schema Registry use the `schema-registry:register` command:
```
 mvn schema-registry:register
 ```
 Additional types can be added via the plugin configuration in pom.xml

To retrieve schemas from a Schema Registry use the `schema-registry:download` command. It will download all schemas matching the `subjectPatterns` parameter to src/main/resources/avro/
