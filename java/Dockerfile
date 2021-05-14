FROM adoptopenjdk:15-jdk-hotspot as builder

WORKDIR /usr/src/

COPY .. /usr/src
RUN ./mvnw package


FROM adoptopenjdk:15-jre-hotspot
WORKDIR /usr/local/bin/

COPY --from=builder /usr/src/consumer/target/demo-consumer-0.0.1-SNAPSHOT.jar ./

COPY .. /app

CMD ["java","-jar","demo-consumer-0.0.1-SNAPSHOT.jar"]
