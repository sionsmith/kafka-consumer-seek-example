# Sample Kafka producer and consumer using Spring Framework

## How to run the sample applications

1. Start up the kafka stack by running in the `docker` directory.
     ```shell
    $ docker-compose -f ./docker/single-broker.yml up 
    ``` 
2. Install the Producer with Maven. 
     ```shell
     $ mvn -f java/producer/pom.xml clean install 
     ``` 
3. Run the Producer. This loads some sample data from an `sample_event.json`
    ```shell
    $ java -jar java/producer/target/demo-producer-0.0.1-SNAPSHOT.jar
    ```
4. Install the Consumer with Maven. 
     ```shell
     $ mvn -f java/consumer/pom.xml clean install 
     ``` 
5. Run the Consumer. The consumer reads a single event from a given offset and partition. This payload is then sent to a lambda function.
    ```shell
    $ export AWS_ACCESS_KEY_ID=xxx
    $ export AWS_SECRET_ACCESS_KEY=xxx
    $ export KAFKA.CONSUMER.ERROR-TOPIC-NAME=shipment-sink-error
    $ export KAFKA.CONSUMER.TOPIC-NAME=shipment-service
    $ export LAMBDA.REGION=eu-west-2
    $ export LAMBDA.FUNCTION-NAME=shipment-process-dev-index
    # run the consumer with the following
    $ java -jar java/consumer/target/demo-consumer-0.0.1-SNAPSHOT.jar
    ```
6. Destroy the environment 
    ```shell
    $ docker-compose -f ./docker/single-broker.yml down -v
    ```

## Kafka configuration

The producer and consumer configs can be set inside `application.yaml` or overriden via environmental variables as shown above.

[Reference - how Spring Boot processes external configuration](https://docs.spring.io/spring-boot/docs/2.4.1/reference/html/spring-boot-features.html#boot-features-external-config)

Reference for all available configuration parameters of the Kafka consumer and producer:

- [Producer configs](https://kafka.apache.org/documentation/#producerconfigs)
- [Consumer configs](https://kafka.apache.org/documentation/#consumerconfigs)

Additional configuration when using the Schema Registry is set in the `KafkaConfig` class.

[Reference - Configuration Options for Schema Registry](https://docs.confluent.io/platform/current/schema-registry/connect.html#configuration-options)

## Appendix
### Useful commands

* List out topics in the cluster 
`kafka-topics --bootstrap-server localhost:9092  --list`

* Send a test event to the error sink topic
`jq -rc . ./error_event.json | kafka-console-producer --broker-list localhost:9092 --topic shipment-sink-error`

