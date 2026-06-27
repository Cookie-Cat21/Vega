FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM flink:1.20-java17
COPY --from=build /build/target/vega-flink-jobs-1.0.0-SNAPSHOT.jar /opt/flink/usrlib/vega-flink-jobs.jar
