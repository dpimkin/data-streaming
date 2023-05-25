package com.example.transform.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.example.transform.Beans.JSON_DATA;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Repository
public class DatasetGeneratorClient {

    @Autowired
    @Qualifier(JSON_DATA)
    WebClient webClient;

    @Value("${client.buffer-size}")
    int bufferSize;

    public Flux<byte[]> fetchDataset(long size, Flux<DataBuffer> fileContentFlux) {
        var s1 = webClient.post().uri("generate/dataset/" + size)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(fileContentFlux.doOnNext(str -> log.info("web-client-sent {}", str.readableByteCount())), DataBuffer.class))
                .retrieve()
                .onStatus(
                        httpStatusCode -> httpStatusCode != OK,
                        clientResponse -> Mono.error(new IllegalStateException("omg")))
                .bodyToFlux(byte[].class);

        return s1;
    }



}
