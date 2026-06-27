FROM confluentinc/cp-kafka-connect:7.6.0

USER root

# Confluent Connect 7.6 ships Java 11; Vega connectors require Java 21 (virtual threads).
RUN curl -fsSL "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.3%2B9/OpenJDK21U-jre_x64_linux_hotspot_21.0.3_9.tar.gz" \
      -o /tmp/jre21.tar.gz \
    && mkdir -p /usr/lib/jvm/temurin-21 \
    && tar -xzf /tmp/jre21.tar.gz -C /usr/lib/jvm/temurin-21 --strip-components=1 \
    && rm /tmp/jre21.tar.gz

ENV JAVA_HOME=/usr/lib/jvm/temurin-21
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY connectors/wikimedia/target/vega-wikimedia-connector-1.0.0-SNAPSHOT.jar \
    /usr/share/java/vega-wikimedia-connector/vega-wikimedia-connector.jar
COPY connectors/eonet/target/vega-eonet-connector-1.0.0-SNAPSHOT.jar \
    /usr/share/java/vega-eonet-connector/vega-eonet-connector.jar
COPY connectors/slnews/target/vega-slnews-connector-1.0.0-SNAPSHOT.jar \
    /usr/share/java/vega-slnews-connector/vega-slnews-connector.jar

USER appuser
