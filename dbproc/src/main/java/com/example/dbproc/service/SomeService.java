package com.example.dbproc.service;

import com.example.dbproc.Some;
import com.example.dbproc.model.SomeDoc;
import com.example.dbproc.model.SomeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SomeService {

    private final SomeRepository someRepository;


    public Flux<SomeDoc> fetch() {
        return someRepository.findAll();
    }

    public Mono<String> process(Flux<Some> stringFlux) {
        return stringFlux.flatMap(str -> {
                    var doc = SomeDoc.of(null, str.getPayload());
                    return someRepository.save(doc);
                })
                .doOnNext(doc -> {
                    log.info("saved {}", doc.getId());
                })
                .last()
                .thenReturn("done");
    }


}
