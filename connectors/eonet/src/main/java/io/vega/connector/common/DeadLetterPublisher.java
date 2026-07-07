package io.vega.connector.common;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public final class DeadLetterPublisher implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeadLetterPublisher.class);
    private static final String DEFAULT_TOPIC = "vega-dead-letter";

    private final KafkaProducer<String, String> producer;
    private final String topic;

    public DeadLetterPublisher(String bootstrapServers) {
        this(bootstrapServers, System.getenv().getOrDefault("VEGA_DLQ_TOPIC", DEFAULT_TOPIC));
    }

    public DeadLetterPublisher(String bootstrapServers, String topic) {
        this.topic = topic;
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        this.producer = new KafkaProducer<>(props);
    }

    public void publish(String source, String rawPayload, String errorMessage) {
        String value = String.format(
                "{\"source\":\"%s\",\"error\":\"%s\",\"payload\":%s,\"timestamp\":%d}",
                escape(source), escape(errorMessage), rawPayload, System.currentTimeMillis());
        producer.send(new ProducerRecord<>(topic, source, value));
        LOG.warn("Published dead-letter event from {}: {}", source, errorMessage);
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public void close() {
        producer.close();
    }
}
