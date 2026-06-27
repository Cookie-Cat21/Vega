package io.vega.flink.models;

public record RawWikiEvent(
        Long id,
        String title,
        String user,
        boolean bot,
        String wiki,
        String serverUrl,
        long timestamp,
        String type,
        int namespace,
        String comment,
        Integer lengthOld,
        Integer lengthNew,
        Long revisionOld,
        Long revisionNew
) {}
