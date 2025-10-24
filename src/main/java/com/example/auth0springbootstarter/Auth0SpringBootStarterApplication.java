package com.example.auth0springbootstarter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class Auth0SpringBootStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(Auth0SpringBootStarterApplication.class, args);
    }

}
