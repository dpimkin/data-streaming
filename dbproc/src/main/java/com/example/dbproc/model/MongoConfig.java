package com.example.dbproc.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.Index;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class MongoConfig {

    private final ReactiveMongoOperations reactiveMongoOperations;


    @Bean
    ApplicationListener<ContextRefreshedEvent> applicationListener(@Value("${dataset.coll}") String collName) {
        return event -> reactiveMongoOperations.indexOps(collName)
                .ensureIndex(new Index().on("payload", Sort.Direction.ASC)
                        //.unique()
                        .named("payload_unique_index")).subscribe(s -> log.info("{} index OK", collName));
    }

}
