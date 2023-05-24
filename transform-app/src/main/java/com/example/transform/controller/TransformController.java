package com.example.transform.controller;

import com.example.transform.repository.DatasetGeneratorClient;
import com.example.transform.service.TransformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.LongAdder;

import static com.example.transform.Beans.ASYNC;
import static com.example.transform.Beans.JSON_DATA;

@Slf4j
@RestController
public class TransformController {

    @Autowired
    DatasetGeneratorClient datasetGeneratorClient;

    @Autowired
    TransformService transformService;

    @Autowired
    @Qualifier(JSON_DATA)
    WebClient webClient;

    @Autowired
    @Qualifier(ASYNC)
    ExecutorService executorService;

    @Value("${transform.buffer-size}")
    int transformBufferSize;

    @GetMapping(value = "/transform/{size}")
    ResponseEntity<Flux<byte[]>> transformData3(@PathVariable("size") long size) {
        if (size < 2) {
            throw new IllegalArgumentException();
        }
        var pipedInputStream = new PipedInputStream();
        LongAdder payload = new LongAdder();
        try {
            var pipedOutputStream = new PipedOutputStream(pipedInputStream);
            executorService.submit(() -> {
                datasetGeneratorClient.fetchDataset(size)
                        .subscribe(bytes -> {
                            try {
                                payload.add(bytes.length);
                                pipedOutputStream.write(bytes);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }, (ex) -> {
                            log.error("something goes wrong", ex);
                        }, () -> {
                            try {
                                pipedOutputStream.close();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            });
            var result = transformService.parse(pipedInputStream);


            Flux<byte[]> fileFlux = Flux.create(sink -> {
                byte[] buffer = new byte[transformBufferSize];
                try (InputStream inputStream = new BufferedInputStream(new FileInputStream(result.file()))) {
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        sink.next(Arrays.copyOf(buffer, bytesRead));
                    }
                    sink.complete();
                } catch (IOException e) {
                    sink.error(e);
                }
            });

            //Flux<byte[]> fileFlux = Flux.defer(() -> Flux.just(readFileBytes(result.file())));
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + result.file().getName() + ".snappy")
                    .body(fileFlux);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }
}
