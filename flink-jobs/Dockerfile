FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests -q

# Official Hub has no flink:1.20-java21; use local-compatible base with JRE 21.
FROM eclipse-temurin:21-jre-jammy AS jre21
FROM flink:1.20.5-java17
USER root
COPY --from=jre21 /opt/java/openjdk /opt/java/java21
ENV JAVA_HOME=/opt/java/java21
ENV PATH="${JAVA_HOME}/bin:${PATH}"
LABEL org.opencontainers.image.title="vega-flink-jobs"
LABEL org.opencontainers.image.source="https://github.com/Cookie-Cat21/Vega"
COPY --from=build /build/target/vega-flink-jobs-1.0.0-SNAPSHOT.jar /opt/flink/usrlib/vega-flink-jobs.jar
USER flink
