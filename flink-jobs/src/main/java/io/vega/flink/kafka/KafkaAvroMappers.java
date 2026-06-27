package io.vega.flink.kafka;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.vega.flink.models.NaturalEvent;
import io.vega.flink.models.RawSLNewsArticle;
import io.vega.flink.models.RawWikiEvent;
import org.apache.avro.generic.GenericRecord;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class KafkaAvroMappers {

    private KafkaAvroMappers() {}

    public static KafkaRecordDeserializationSchema<RawWikiEvent> wikiEventDeserializer(String schemaRegistryUrl) {
        return new WikiEventKafkaDeserializer(schemaRegistryUrl);
    }

    public static KafkaRecordDeserializationSchema<NaturalEvent> naturalEventDeserializer(String schemaRegistryUrl) {
        return new NaturalEventKafkaDeserializer(schemaRegistryUrl);
    }

    public static KafkaRecordDeserializationSchema<RawSLNewsArticle> slNewsDeserializer(String schemaRegistryUrl) {
        return new SLNewsKafkaDeserializer(schemaRegistryUrl);
    }

    private static Map<String, Object> avroConfig(String schemaRegistryUrl) {
        Map<String, Object> config = new HashMap<>();
        config.put("schema.registry.url", schemaRegistryUrl);
        config.put("specific.avro.reader", false);
        return config;
    }

    private static final class WikiEventKafkaDeserializer implements KafkaRecordDeserializationSchema<RawWikiEvent> {
        private final String schemaRegistryUrl;
        private transient KafkaAvroDeserializer deserializer;

        WikiEventKafkaDeserializer(String schemaRegistryUrl) {
            this.schemaRegistryUrl = schemaRegistryUrl;
        }

        @Override
        public void open(DeserializationSchema.InitializationContext context) {
            deserializer = new KafkaAvroDeserializer();
            deserializer.configure(avroConfig(schemaRegistryUrl), false);
        }

        @Override
        public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<RawWikiEvent> out) {
            GenericRecord avro = (GenericRecord) deserializer.deserialize(record.topic(), record.value());
            if (avro != null) {
                out.collect(mapWikiEvent(avro));
            }
        }

        @Override
        public TypeInformation<RawWikiEvent> getProducedType() {
            return TypeInformation.of(RawWikiEvent.class);
        }
    }

    private static final class NaturalEventKafkaDeserializer implements KafkaRecordDeserializationSchema<NaturalEvent> {
        private final String schemaRegistryUrl;
        private transient KafkaAvroDeserializer deserializer;

        NaturalEventKafkaDeserializer(String schemaRegistryUrl) {
            this.schemaRegistryUrl = schemaRegistryUrl;
        }

        @Override
        public void open(DeserializationSchema.InitializationContext context) {
            deserializer = new KafkaAvroDeserializer();
            deserializer.configure(avroConfig(schemaRegistryUrl), false);
        }

        @Override
        public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<NaturalEvent> out) {
            GenericRecord avro = (GenericRecord) deserializer.deserialize(record.topic(), record.value());
            if (avro != null) {
                out.collect(mapNaturalEvent(avro));
            }
        }

        @Override
        public TypeInformation<NaturalEvent> getProducedType() {
            return TypeInformation.of(NaturalEvent.class);
        }
    }

    static RawWikiEvent mapWikiEvent(GenericRecord r) {
        return new RawWikiEvent(
                r.get("id") != null ? ((Number) r.get("id")).longValue() : null,
                stringVal(r, "title"),
                stringVal(r, "user"),
                Boolean.TRUE.equals(r.get("bot")),
                stringVal(r, "wiki"),
                stringVal(r, "server_url"),
                ((Number) r.get("timestamp")).longValue(),
                stringVal(r, "type"),
                ((Number) r.get("namespace")).intValue(),
                r.get("comment") != null ? r.get("comment").toString() : null,
                r.get("length_old") != null ? ((Number) r.get("length_old")).intValue() : null,
                r.get("length_new") != null ? ((Number) r.get("length_new")).intValue() : null,
                r.get("revision_old") != null ? ((Number) r.get("revision_old")).longValue() : null,
                r.get("revision_new") != null ? ((Number) r.get("revision_new")).longValue() : null
        );
    }

    static NaturalEvent mapNaturalEvent(GenericRecord r) {
        return new NaturalEvent(
                stringVal(r, "event_id"),
                stringVal(r, "title"),
                r.get("description") != null ? r.get("description").toString() : null,
                stringVal(r, "category"),
                r.get("source_url") != null ? r.get("source_url").toString() : null,
                ((Number) r.get("latitude")).doubleValue(),
                ((Number) r.get("longitude")).doubleValue(),
                ((Number) r.get("event_date")).longValue(),
                r.get("magnitude_value") != null ? ((Number) r.get("magnitude_value")).doubleValue() : null,
                r.get("magnitude_unit") != null ? r.get("magnitude_unit").toString() : null,
                Boolean.TRUE.equals(r.get("is_closed")),
                ((Number) r.get("ingested_at")).longValue()
        );
    }

    private static final class SLNewsKafkaDeserializer implements KafkaRecordDeserializationSchema<RawSLNewsArticle> {
        private final String schemaRegistryUrl;
        private transient KafkaAvroDeserializer deserializer;

        SLNewsKafkaDeserializer(String schemaRegistryUrl) {
            this.schemaRegistryUrl = schemaRegistryUrl;
        }

        @Override
        public void open(DeserializationSchema.InitializationContext context) {
            deserializer = new KafkaAvroDeserializer();
            deserializer.configure(avroConfig(schemaRegistryUrl), false);
        }

        @Override
        public void deserialize(ConsumerRecord<byte[], byte[]> record, Collector<RawSLNewsArticle> out) {
            GenericRecord avro = (GenericRecord) deserializer.deserialize(record.topic(), record.value());
            if (avro != null) {
                out.collect(mapSLNewsArticle(avro));
            }
        }

        @Override
        public TypeInformation<RawSLNewsArticle> getProducedType() {
            return TypeInformation.of(RawSLNewsArticle.class);
        }
    }

    static RawSLNewsArticle mapSLNewsArticle(GenericRecord r) {
        return new RawSLNewsArticle(
                stringVal(r, "article_id"),
                stringVal(r, "title"),
                r.get("description") != null ? r.get("description").toString() : null,
                stringVal(r, "link"),
                stringVal(r, "source_feed"),
                stringVal(r, "source_name"),
                ((Number) r.get("published_at")).longValue(),
                ((Number) r.get("ingested_at")).longValue(),
                stringVal(r, "language")
        );
    }

    private static String stringVal(GenericRecord r, String field) {
        Object val = r.get(field);
        return val != null ? val.toString() : "";
    }
}
