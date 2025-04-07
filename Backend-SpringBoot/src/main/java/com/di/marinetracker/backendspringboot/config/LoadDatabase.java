package com.di.marinetracker.backendspringboot.config;

import com.di.marinetracker.backendspringboot.vessels.Vessel;
import com.di.marinetracker.backendspringboot.vessels.VesselRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(VesselRepository repository) {

        return args -> {
            log.info("Preloading " + repository.save(new Vessel("99239923", "cargo")));
            log.info("Preloading " + repository.save(new Vessel("38883838", "fishing")));
        };
    }
}
