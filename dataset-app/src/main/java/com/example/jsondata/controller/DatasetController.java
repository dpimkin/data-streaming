package com.example.jsondata.controller;

import com.example.jsondata.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Controller class for handling dataset-related requests.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;

    /**
     * Generates a large output JSON dataset.
     *
     * @param size The size of the dataset to generate (should be more than 1).
     * @return A Flux of strings representing the generated dataset.
     * @throws IllegalArgumentException if the size is less than 1.
     */
    @PostMapping(value = "/dataset/{size}", produces = MediaType.APPLICATION_JSON_VALUE)
    Flux<String> getLargeOutputJson(@PathVariable("size") long size) {
        if (size < 2) {
            throw new IllegalArgumentException();
        }
        log.debug("generates {} records", size);
        return datasetService.generateDataset(size);
    }
}

