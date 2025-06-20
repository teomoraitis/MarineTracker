package com.di.marinetracker.backendspringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// This is the entry point for the Spring Boot application.
// In the entire Spring Boot framework, Inversion of Control (IoC) is always present.
// The framework transparently manages the creation, configuration, and wiring of components (beans).
// The @SpringBootApplication annotation triggers component scanning and auto-configuration.
@SpringBootApplication
public class BackendSpringBootApplication {

    public static void main(String[] args) {
        // This starts the Spring IoC container, which manages the application's flow and dependencies.
        SpringApplication.run(BackendSpringBootApplication.class, args);
        System.out.println("Starting the Backend for the MarineTracker Web Platform.");

    }

}
