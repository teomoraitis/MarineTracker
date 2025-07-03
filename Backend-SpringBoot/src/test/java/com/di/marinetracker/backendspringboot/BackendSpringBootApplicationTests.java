package com.di.marinetracker.backendspringboot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendSpringBootApplicationTests {

    // Test for general app context loading
    @Test
    void contextLoads() {
        // Verifies if Spring context loads successfully
    }

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${kafka.topic}")
    private String kafkaTopic;

    @Value("${kafka.bootstrapserver}")
    private String kafkaBootstrapServer;

    @Value("${server.port}")
    private int serverPort;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${com.di.marinetracker.backendspringboot.jwtSecret}")
    private String jwtSecret;

    @Value("${com.di.marinetracker.backendspringboot.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Test to verify that application properties are loaded correctly
    @Test
    void testApplicationProperties() {
        System.out.println("Application Name: " + applicationName);
        System.out.println("Kafka Topic: " + kafkaTopic);
        System.out.println("Kafka Bootstrap Server: " + kafkaBootstrapServer);
        System.out.println("Server Port: " + serverPort);
        System.out.println("Datasource URL: " + datasourceUrl);
        System.out.println("Datasource Username: " + datasourceUsername);
        System.out.println("Datasource Password: " + datasourcePassword);
        System.out.println("JWT Secret: " + jwtSecret);
        System.out.println("JWT Expiration (ms): " + jwtExpirationMs);
    }
}