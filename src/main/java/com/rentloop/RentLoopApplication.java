package com.rentloop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RentLoopApplication {
    public static void main(String[] args) {
        SpringApplication.run(RentLoopApplication.class, args);
    }
}
