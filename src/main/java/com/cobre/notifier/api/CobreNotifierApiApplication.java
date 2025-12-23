package com.cobre.notifier.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CobreNotifierApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CobreNotifierApiApplication.class, args);
    }
}


