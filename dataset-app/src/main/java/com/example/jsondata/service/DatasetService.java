package com.example.jsondata.service;


import com.example.jsondata.domain.MetadataDTO;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

import static com.example.jsondata.service.DatasetMetadata.TIMESTAMP_FORMAT;

/**
 * The DatasetService class is responsible for generating a JSON dataset based on the specified size.
 * It utilizes the provided JsonFactory, DatasetMetadata, and ObjectMapper to generate the dataset.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatasetService {
    private final JsonFactory jsonFactory;
    private final DatasetMetadata datasetMetadata;
    private final ObjectMapper objectMapper;

    /**
     * Generates a JSON dataset with the given size.
     *
     * @param size The desired size of the dataset.
     * @return A Flux of strings representing the generated dataset.
     * @throws IllegalArgumentException if the size is less than 2.
     */
    public Flux<String> generateDataset(final long size) {
        if (size < 2) {
            throw new IllegalArgumentException();
        }
        var start = LocalDateTime.now();
        var formatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);
        var generatedEntries = new AtomicLong(0L);
        var counter = new LongAdder();

        return Flux.fromStream(Stream.generate(() -> {
            try {
                var writer = new StringWriter();
                final var first = generatedEntries.get() == 0;

                var random = ThreadLocalRandom.current();
                try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer)) {
                    jsonGenerator.writeStartObject();


                    for (var fieldName : datasetMetadata.getStringFields()) {
                        jsonGenerator.writeStringField(fieldName, "some_string_value_" + (byte) random.nextInt(1, 64));
                    }

                    for (var fieldName : datasetMetadata.getDoubleFields()) {
                        jsonGenerator.writeFieldName(fieldName);
                        jsonGenerator.writeNumber(random.nextDouble());
                    }

                    for (var fieldName : datasetMetadata.getIntFields()) {
                        jsonGenerator.writeFieldName(fieldName);
                        jsonGenerator.writeNumber(random.nextLong());
                    }

                    for (var fieldName : datasetMetadata.getBooleanFields()) {
                        jsonGenerator.writeFieldName(fieldName);
                        jsonGenerator.writeBoolean(random.nextBoolean());
                    }

                    jsonGenerator.writeEndObject();
                }
                final var last = size == generatedEntries.incrementAndGet();


                if (!first && !last) {
                    var result = writer + ",";
                    counter.add(result.length());
                    log.info("generated {} KB", counter.longValue() / 1024);
                    return result;
                } else if (first) {
                    var result = "{\n" +
                            "  \"Response\": {\n" +
                            "    \"pricingLineList\": [" + writer + ",";
                    counter.add(result.length());
                    log.info("generated {} KB", counter.longValue() / 1024);
                    return result;

                } else {
                    var result = writer + "]\n" +
                            "  },\n" +
                            "  \"MetaData\": " +
                            objectMapper.writeValueAsString(MetadataDTO.random()) +
                            ",\n" +
                            "  \"RequestTime\": \"" + formatter.format(start) + "\",\n" +
                            "  \"ResponseTime\": \"" + "\",\n" +
                            "  \"ErrorCode\": \"\",\n" +
                            "  \"ErrorMessage\": \"\"\n" +
                            "}";
                    counter.add(result.length());
                    log.info("generated {} KB", counter.longValue() / 1024);
                    return result;

                }
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }).limit(size));
    }
}
