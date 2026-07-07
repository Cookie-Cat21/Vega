FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

FROM flink:1.20-java21
LABEL org.opencontainers.image.title="vega-flink-jobs"
LABEL org.opencontainers.image.source="https://github.com/Cookie-Cat21/Vega"
COPY --from=build /build/target/vega-flink-jobs-1.0.0-SNAPSHOT.jar /opt/flink/usrlib/vega-flink-jobs.jar
