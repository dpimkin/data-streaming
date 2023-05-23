package com.example.jsondata;

import com.fasterxml.jackson.core.JsonFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(proxyBeanMethods = false)
public class DatasetApp {

    public static void main(String[] args) {
        SpringApplication.run(DatasetApp.class, args);
    }

    @Bean
    JsonFactory jsonFactory() {
        return new JsonFactory();
    }

}
