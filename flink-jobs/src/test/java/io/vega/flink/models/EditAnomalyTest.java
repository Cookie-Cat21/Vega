package io.vega.flink.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditAnomalyTest {

    @Test
    void anomalyTypeEnumValues() {
        assertEquals(EditAnomaly.AnomalyType.LARGE_EDIT,
                EditAnomaly.AnomalyType.valueOf("LARGE_EDIT"));
    }

    @Test
    void recordHoldsAnomalyFields() {
        EditAnomaly anomaly = new EditAnomaly(
                "Earth", "vandal", "enwiki", 1_700_000_000_000L,
                EditAnomaly.AnomalyType.LARGE_EDIT, 5000, 1);

        assertEquals(EditAnomaly.AnomalyType.LARGE_EDIT, anomaly.anomalyType());
        assertEquals(5000, anomaly.editSizeDelta());
    }
}
