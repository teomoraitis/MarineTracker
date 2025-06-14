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

    @Autowired
    PasswordEncoder encoder;

    @Bean
    CommandLineRunner initUserDatabase(UserRepository repository) {
        return args -> {
            log.info("Preloading " + repository.save(new User("admin", "admin@example.com", encoder.encode("123"), Set.of("user", "admin"))));
        };
    }
}
