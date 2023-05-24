package com.example.dbproc.controller;

import com.example.dbproc.Some;
import com.example.dbproc.model.SomeDoc;
import com.example.dbproc.service.SomeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SomeController {

    private final SomeService someService;

    @PostMapping("/info")
    Mono<? extends ResponseEntity<?>> store(@RequestBody Flux<Some> body) {
        log.info("storing...");
        return someService.process(body).map(v -> ResponseEntity.ok().build());
    }

    @GetMapping(value = "all", produces = MediaType.APPLICATION_NDJSON_VALUE )
    Flux<SomeDoc> fetch() {
        log.info("fetching...");
        return someService.fetch();
    }
}
