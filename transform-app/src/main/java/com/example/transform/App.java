package com.example.transform;

import com.fasterxml.jackson.core.JsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.transform.Beans.ASYNC;
import static com.example.transform.Beans.JSON_DATA;
import static java.util.Objects.requireNonNull;

@Slf4j
@SpringBootApplication(proxyBeanMethods = false)
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	@Qualifier(JSON_DATA)
	WebClient jsonWebClient(@Value("${client.base-url}") String baseUrl) {
		log.info("client base url {}", baseUrl);
		return WebClient.builder()
				.baseUrl(requireNonNull(baseUrl))
				.build();
	}


	@Bean
	JsonFactory jsonFactory() {
		return new JsonFactory();
	}

	@Bean
	@Qualifier(ASYNC)
	ExecutorService executorService() {
		return Executors.newCachedThreadPool();
	}
}
