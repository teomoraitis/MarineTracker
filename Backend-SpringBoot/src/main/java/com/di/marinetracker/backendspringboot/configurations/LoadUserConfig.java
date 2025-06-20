package com.di.marinetracker.backendspringboot.configurations;

import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class LoadUserConfig {

    private static final Logger log = LoggerFactory.getLogger(LoadUserConfig.class);

    // Injects the PasswordEncoder bean for encoding user passwords
    @Autowired
    PasswordEncoder encoder;

    // Bean that runs on application startup to preload the database with a default admin user
    @Bean
    CommandLineRunner initUserDatabase(UserRepository repository) {
        return args -> {
            // Saves a new User entity with username 'admin', email, encoded password, and roles:
            log.info("Preloading " + repository.save(new User("admin", "admin@example.com", encoder.encode("123"), Set.of("user", "admin"))));
        };
    }
}
