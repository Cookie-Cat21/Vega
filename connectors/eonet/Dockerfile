FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM confluentinc/cp-kafka-connect:7.6.0
COPY --from=build /build/target/vega-eonet-connector-1.0.0-SNAPSHOT.jar \
    /usr/share/java/vega-eonet-connector/vega-eonet-connector.jar
