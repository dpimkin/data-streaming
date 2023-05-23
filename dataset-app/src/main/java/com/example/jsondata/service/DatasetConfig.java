package com.example.jsondata.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Configuration(proxyBeanMethods = false)
public class DatasetConfig {


    /**
     * The parameter value for string fields.
     */
    @Value("${dataset-meta.string-fields:}")
    String stringFieldsParam;

    /**
     * The parameter value for double fields.
     */
    @Value("${dataset-meta.double-fields:}")
    String doubleFieldsParam;

    /**
     * The parameter value for integer fields.
     */
    @Value("${dataset-meta.int-fields:}")
    String intFieldsParam;

    /**
     * The parameter value for boolean fields.
     */
    @Value("${dataset-meta.boolean-fields:}")
    String booleanFieldsParam;

    /**
     * Creates and returns the dataset metadata bean.
     * @return The dataset metadata bean.
     */
    @Bean
    DatasetMetadata datasetMetadata() {
        final var stringFields = split(Objects.requireNonNull(stringFieldsParam));
        final var doubleFields = split(Objects.requireNonNull(doubleFieldsParam));
        final var intFields = split(Objects.requireNonNull(intFieldsParam));
        final var booleanFields = split(Objects.requireNonNull(booleanFieldsParam));
        ensureSetsUnique(stringFields, doubleFields, intFields, booleanFields);
        return new DatasetMetadata(stringFields.stream().toList(),
                doubleFields.stream().toList(),
                intFields.stream().toList(),
                booleanFields.stream().toList());
    }


    /**
     * Ensures that all elements in the provided sets are unique.
     *
     * @param sets The sets to be checked for uniqueness.
     * @throws IllegalArgumentException if any element is found to be non-unique.
     */
    public static void ensureSetsUnique(Set<?>... sets) {
        var allElements = new HashSet<>();
        for (Set<?> set : sets) {
            for (Object element : set) {
                if (!allElements.add(element)) {
                    throw new IllegalArgumentException("please use unique names for columns. \"" + element + "\n isn't unique");
                }
            }
        }
    }


    /**
     * Splits the given fields parameter into a set of strings.
     *
     * @param fieldsParam The string containing the fields parameter to be split.
     * @return A set of strings resulting from splitting the fields parameter.
     */
    private Set<String> split(String fieldsParam) {
        return Arrays.stream(fieldsParam.split(","))
                .map(String::trim)
                .filter(fieldName -> fieldName.length() >= 1)
                .collect(Collectors.toSet());

    }


}
