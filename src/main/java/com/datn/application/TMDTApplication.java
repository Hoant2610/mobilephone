package com.datn.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class TMDTApplication {

    public static void main(String[] args) {
        SpringApplication.run(TMDTApplication.class, args);
    }

}
