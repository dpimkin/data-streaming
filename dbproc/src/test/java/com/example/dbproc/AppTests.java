package com.example.dbproc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import com.example.dbproc.model.SomeDoc;

import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class AppTests {

    @Container
    private static MongoDBContainer MONGODB = new MongoDBContainer();

    @Autowired
    WebTestClient webTestClient;


    @Test
    void contextLoads() {
        Flux<Some> someFlux = Flux.just(Some.of("foo"));

        webTestClient.post()
                .uri("/info")
                .body(someFlux, Some.class)
                .exchange()
                .expectStatus()
                .isOk();

        var result = webTestClient.get()
                .uri("/all")
                .exchange()
                .expectStatus()
                .isOk()

                .expectBodyList(SomeDoc.class)
                .returnResult()
                .getResponseBody();

        assertTrue(result.stream().anyMatch(dto ->
                "foo".equals(dto.getPayload())));
    }

    @DynamicPropertySource
    static void configureMongodb(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGODB::getReplicaSetUrl);
    }

}
