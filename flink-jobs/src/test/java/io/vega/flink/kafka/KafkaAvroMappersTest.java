package io.vega.flink.kafka;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class KafkaAvroMappersTest {

    @Test
    void mapsWikiEventFromGenericRecord() {
        Schema schema = SchemaBuilder.record("WikiEvent")
                .fields()
                .optionalLong("id")
                .requiredString("title")
                .requiredString("user")
                .requiredBoolean("bot")
                .requiredString("wiki")
                .requiredString("server_url")
                .requiredLong("timestamp")
                .requiredString("type")
                .requiredInt("namespace")
                .optionalString("comment")
                .optionalInt("length_old")
                .optionalInt("length_new")
                .optionalLong("revision_old")
                .optionalLong("revision_new")
                .endRecord();

        GenericRecord record = new GenericData.Record(schema);
        record.put("id", 42L);
        record.put("title", "Earth");
        record.put("user", "alice");
        record.put("bot", false);
        record.put("wiki", "enwiki");
        record.put("server_url", "https://en.wikipedia.org");
        record.put("timestamp", 1_700_000_000_000L);
        record.put("type", "edit");
        record.put("namespace", 0);
        record.put("length_old", 100);
        record.put("length_new", 200);

        var mapped = KafkaAvroMappers.mapWikiEvent(record);

        assertEquals("Earth", mapped.title());
        assertEquals("enwiki", mapped.wiki());
        assertFalse(mapped.bot());
    }
}
