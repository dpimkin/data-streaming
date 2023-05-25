package com.example.jsondata.controller;

import com.example.jsondata.service.DatasetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.atomic.LongAdder;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller class for handling dataset-related requests.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetService datasetService;


    @Value("${io.buffer.size:8192}")
    private int ioBufferSize;

    /**
     * Generates a large output JSON dataset.
     *
     * @param size The size of the dataset to generate (should be more than 2).
     * @return A Flux of strings representing the generated dataset.
     * @throws IllegalArgumentException if the size is less than 1.
     */
    @PostMapping(value = "/dataset/{size}",
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    Flux<String> getLargeOutputJson(@PathVariable("size") long size) {
        if (size < 2) {
            throw new IllegalArgumentException();
        }
        log.debug("generates {} records", size);
        return datasetService.generateDataset(size);
    }

    @PostMapping(
            value = "/generate/dataset/{size}",
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    Flux<String> generateDateset(@PathVariable("size") long size,
                                 @RequestBody Flux<byte[]> byteArrayFlux) {
        byteArrayFlux.subscribe(bytes -> log.info("consumed body part size {}", bytes.length));
        return datasetService.generateDataset(size);
    }


    @PostMapping(value = "/generate-error/{error-code:400|500}",
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    ResponseEntity<?> generateError(@PathVariable("error-code") int errorCode,
                                    @RequestBody Flux<byte[]> bodyFlux) {
        return ResponseEntity.status(errorCode).build();
    }


    /**
     * Replies with predefined json content
     *
     *  <pre>curl http://localhost:8081/dataset/predefined -H "Content-type: application/json" -d "[]"</pre>
     */
    @PostMapping(value = "/dataset/predefined", produces = APPLICATION_JSON_VALUE)
    Flux<byte[]> getPredefinedJson() {
        var counter = new LongAdder();
        Flux<byte[]> fileFlux = Flux.create(sink -> {
            byte[] buffer = new byte[ioBufferSize];

            try (InputStream is = DatasetController.class.getResourceAsStream("/predefined_response.json")) {
                try (BufferedInputStream inputStream = new BufferedInputStream(is)) {
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        counter.add(bytesRead);
                        sink.next(Arrays.copyOf(buffer, bytesRead));
                    }
                    log.info("generated {} KB", counter.longValue() / 1024);
                    sink.complete();
                }
            } catch (IOException e) {
                sink.error(e);
            }
            log.info("total {} KB", counter.longValue() / 1024);
        });

        return fileFlux;
    }


}

