FROM maven:3.6-jdk-11-slim as builder
WORKDIR /usr/src/

COPY . /usr/src

RUN mvn package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /usr/src/consumer/target/demo-consumer-0.0.1-SNAPSHOT.jar ./

CMD ["java","-jar","demo-consumer-0.0.1-SNAPSHOT.jar"]
