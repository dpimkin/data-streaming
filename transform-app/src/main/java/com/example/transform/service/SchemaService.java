package com.example.transform.service;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * A service class for generating schema based on the provided columns.
 */
@Slf4j
@Service
public class SchemaService {

    /**
     * Generates a schema in JSON format based on the provided columns.
     *
     * @param columns a map of column names and their corresponding data types
     * @return the generated schema in JSON format
     * @throws IllegalStateException if an unsupported data type is encountered
     */
    @Nonnull
    public String generateSchema(@Nonnull HashMap<String, Class<?>> columns) {
        var cols = columns.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> {
                    if (entry.getValue().equals(Double.class)) {
                        return "{\"name\": \""  + entry.getKey() + "\", \"type\": [\"null\", \"double\"], \"default\": null}";
                    } else if (entry.getValue().equals(String.class)) {
                        return "{\"name\": \""  + entry.getKey() + "\", \"type\": [\"null\", \"string\"], \"default\": null}";
                    } else if (entry.getValue().equals(Long.class)) {
                        return "{\"name\": \""  + entry.getKey() + "\", \"type\": [\"null\", \"long\"], \"default\": null}";
                    } else if (entry.getValue().equals(Integer.class)) {
                        return "{\"name\": \""  + entry.getKey() + "\", \"type\": [\"null\", \"int\"], \"default\": null}";
                    } else if (entry.getValue().equals(Boolean.class)) {
                        return "{\"name\": \""  + entry.getKey() + "\", \"type\": [\"null\", \"boolean\"], \"default\": null}";
                    }
                    throw new IllegalStateException("unsupported type");
                }).collect(Collectors.joining(","));

        return "{\n" +
                "   \"type\": \"record\",\n" +
                "   \"name\": \"name\",\n" +
                "   \"namespace\": \"namespace\",\n" +
                "   \"fields\": [" + cols + "   ]\n" +
                "}";
    }
}
