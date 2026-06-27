package io.vega.connector.wikimedia;

import io.confluent.connect.avro.AvroData;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;

final class AvroConnect {

    private static final AvroData AVRO = new AvroData(100);

    private AvroConnect() {
    }

    static SchemaAndValue toConnect(SpecificRecord record) {
        org.apache.avro.Schema avroSchema = record.getSchema();
        Object data = AVRO.toConnectData(avroSchema, record);
        if (data instanceof SchemaAndValue schemaAndValue) {
            return schemaAndValue;
        }
        Schema connectSchema = AVRO.toConnectSchema(avroSchema);
        return new SchemaAndValue(connectSchema, data);
    }
}
