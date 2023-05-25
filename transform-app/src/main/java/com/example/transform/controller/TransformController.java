package com.example.transform.controller;

import com.example.transform.repository.DatasetGeneratorClient;
import com.example.transform.repository.FileProcessing;
import com.example.transform.service.TransformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;

import static com.example.transform.Beans.ASYNC;
import static com.example.transform.Beans.JSON_DATA;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TransformController {
    private final DatasetGeneratorClient datasetGeneratorClient;
    private final TransformService transformService;
    private final FileProcessing fileProcessing;


    @Autowired
    @Qualifier(JSON_DATA)
    WebClient webClient;

    @Autowired
    @Qualifier(ASYNC)
    ExecutorService executorService;

    @Value("${transform.buffer-size}")
    int bufferSize;


    @GetMapping(value = "/transform/{codec:SNAPPY|GZIP|ZSTD}/{size}")
    ResponseEntity<? extends Flux<byte[]>> transformData(@PathVariable("codec") String codec, @PathVariable("size") long size) {
        if (size < 2) {
            throw new IllegalArgumentException();
        }

        var compressionCodec = CompressionCodecName.valueOf(codec);
        var pipedInputStream = new PipedInputStream();
        LongAdder payload = new LongAdder();
        try {
            var pipedOutputStream = new PipedOutputStream(pipedInputStream);
            executorService.submit(() -> {
                datasetGeneratorClient.fetchDataset(size, fileProcessing.loadRequest(Paths.get("./request.json"))).subscribe(bytes -> {
                    try {
                        payload.add(bytes.length);
                        pipedOutputStream.write(bytes);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }, (ex) -> log.error("something goes wrong", ex), () -> {
                    try {
                        pipedOutputStream.close();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            });
            Flux<byte[]> fileFlux = Flux.create(sink -> {
                try {
                    log.info("_7");
                    var result = transformService.parse(pipedInputStream, compressionCodec);
                    byte[] buffer = new byte[bufferSize];
                    try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(result.file()))) {
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            sink.next(Arrays.copyOf(buffer, bytesRead));
                        }
                        sink.complete();
                    }
                } catch (IOException e) {
                    sink.error(e);
                }
            });
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + ThreadLocalRandom.current().nextInt() + compressionCodec, compressionCodec.getExtension())
                    .body(fileFlux);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }


}
