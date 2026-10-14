package com.pragma.powerup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OnClassApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnClassApplication.class, args);
    }
}
