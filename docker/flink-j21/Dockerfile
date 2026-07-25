# Flink 1.20 has no official *-java21 Hub tag. Layer Temurin 21 onto 1.20-java17
# so Java 21-compiled Vega jobs can run locally.
FROM eclipse-temurin:21-jre-jammy AS jre21

FROM flink:1.20.5-java17
USER root
COPY --from=jre21 /opt/java/openjdk /opt/java/java21
ENV JAVA_HOME=/opt/java/java21
ENV PATH="${JAVA_HOME}/bin:${PATH}"
USER flink
