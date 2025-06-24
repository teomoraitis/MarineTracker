package com.di.marinetracker.backendspringboot.configurations;

import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.repositories.VesselRepository;
import com.di.marinetracker.backendspringboot.services.VesselPositionsConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
class LoadDatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabaseConfig.class);

    // Inject the VesselPositionsConsumer to start consuming vessel position updates right after preloading the database
    @Autowired
    private VesselPositionsConsumer vesselPositionsConsumer;

    // Bean that runs on application startup to preload the database with vessel data from a CSV file
    @Bean
    CommandLineRunner initDatabase(VesselRepository repository) {

        return args -> {
            // Reads lines from the CSV file containing vessel data
            try (Stream<String> lines = Files.lines(Paths.get("src/main/resources/csv/vessel_types_names.csv"))) {
                // Splits each line by comma and collects as a list of records
                List<List<String>> records = lines.map(line -> Arrays.asList(line.split(",")))
                        .toList();

                System.out.println(); // Newline
                log.info("\n...\n" + "Preloading Database with Static vessel data from vessel_types_names.csv\n" + "...");
                // Iterates over each record and saves a new Vessel entity to the repository
                int total = records.size();
                for (int i = 0; i < total; i++) {
                    List<String> record = records.get(i);
                    String mmsi = (record.size() >= 1) ? record.get(0) : "";
                    String type = (record.size() >= 2) ? record.get(1) : "";
                    String name = (record.size() >= 3) ? record.get(2) : "";
                    repository.save(new Vessel(mmsi, type, name));

                    // Visual progress bar for preloading
                    int progress = (int) (((i + 1) / (double) total) * 50);
                    String bar = "[" + "=".repeat(progress) + " ".repeat(50 - progress) + "]";
                    System.out.print("\r" + bar + " " + (i + 1) + "/" + total);
                }
                System.out.println();
                log.info("\n...\n" + "Preloading complete. Loaded {} vessels.\n" + "...", records.size());
                System.out.println(); // Newline

                // Start the Kafka listener to consume vessel position updates
                vesselPositionsConsumer.startKafkaListener();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
