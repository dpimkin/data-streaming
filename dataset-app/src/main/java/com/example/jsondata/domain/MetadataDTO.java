package com.example.jsondata.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.jsondata.service.DatasetMetadata.TIMESTAMP_FORMAT;

@Data
public class MetadataDTO {

    @JsonProperty("KeyName")
    String keyName;

    @JsonProperty("KeyStartTime")
    String keyStartTime;

    @JsonProperty("ScheduleId")
    String scheduleId;

    @JsonProperty("MasterSetId")
    String masterSetId;

    @JsonProperty("MasterSetRevision")
    int masterSetRevision;

    @JsonProperty("ConfigSetId")
    String configSetId;

    @JsonProperty("ModelRevisionId")
    String modelRevisionId;

    public static MetadataDTO random() {
        var result = new MetadataDTO();
        var random = ThreadLocalRandom.current();
        result.setKeyName("foobar_" + random.nextInt(0, 42));
        result.setKeyStartTime(DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT).format(LocalDateTime.now()));
        result.setScheduleId(UUID.randomUUID().toString());
        result.setMasterSetId(UUID.randomUUID().toString());
        result.setMasterSetRevision(random.nextInt(0, 1024));
        result.setConfigSetId(UUID.randomUUID().toString());
        result.setModelRevisionId(UUID.randomUUID().toString());
        return result;
    }
}
