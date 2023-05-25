package com.example.transform.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.nio.file.Path;

@Repository
@RequiredArgsConstructor
public class FileProcessing {

    private final DataBufferFactory dataBufferFactory;

    @Value("${transform.buffer-size}")
    int transformBufferSize;

    public Flux<DataBuffer> loadRequest(Path file) {
        return DataBufferUtils.read(file, dataBufferFactory, transformBufferSize);
    }


}
