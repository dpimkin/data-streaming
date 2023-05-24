package com.example.transform.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import static com.example.transform.Beans.JSON_DATA;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Repository
public class DatasetGeneratorClient {

    @Autowired
    @Qualifier(JSON_DATA)
    WebClient webClient;

    @Value("${client.buffer-size}")
    int bufferSize;

    public Flux<byte[]> fetchDataset(long size) {
        return webClient.post().uri("dataset/predefined")
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(byte[].class)
                .buffer(bufferSize)
                .flatMap(Flux::fromIterable);
    }
}
