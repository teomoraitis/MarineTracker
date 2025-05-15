package com.di.marinetracker.backendspringboot.config;

import com.di.marinetracker.backendspringboot.vessels.Vessel;
import com.di.marinetracker.backendspringboot.vessels.VesselRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(VesselRepository repository) {

        return args -> {
            try (Stream<String> lines = Files.lines(Paths.get("src/main/resources/csv/vessel_types_names.csv"))) {
                List<List<String>> records = lines.map(line -> Arrays.asList(line.split(",")))
                        .toList();

                for (List<String> record : records) {
                    String mmsi = (record.size() >= 1) ? record.get(0) : "";
                    String type = (record.size() >= 2) ? record.get(1) : "";
                    String name = (record.size() >= 3) ? record.get(2) : "";
                    log.info("Preloading " + repository.save(new Vessel(mmsi, type, name)));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
