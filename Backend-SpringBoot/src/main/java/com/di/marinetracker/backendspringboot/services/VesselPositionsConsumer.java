package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import com.di.marinetracker.backendspringboot.repositories.VesselRepository;
import com.di.marinetracker.backendspringboot.repositories.VesselPositionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

// Service that consumes vessel position updates from Kafka
// and saves them to the database, while also calling the WebSocketService
@Service
public class VesselPositionsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(VesselPositionsConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final VesselRepository vesselRepository;
    private final VesselPositionRepository vesselPositionRepository;
    private final WebSocketService webSocketService;

    // Constructor injection for dependencies (instead of using @Autowired)
    public VesselPositionsConsumer(VesselRepository vesselRepository,
                                   VesselPositionRepository vesselPositionRepository,
                                   WebSocketService webSocketService) {
        this.vesselRepository = vesselRepository;
        this.vesselPositionRepository = vesselPositionRepository;
        this.webSocketService = webSocketService;
    }

    // Consumes messages from the Kafka topic specified in application properties
    @Transactional
    @KafkaListener(topics = "${kafka.topic}", groupId = "ships-consumer")
    public void consume(String message) {
        try {
            // Parse incoming JSON message to a Map
            @SuppressWarnings("unchecked")
            Map<String, Object> positionData = objectMapper.readValue(message, Map.class);

            // Convert MMSI to String (it comes as Integer from Kafka)
            String mmsi = String.valueOf(positionData.get("mmsi"));

            // Check if vessel exists in our database (it should, via static load of name+mmsi+type)
            Optional<Vessel> existingVessel = vesselRepository.findById(mmsi);

            if (!existingVessel.isPresent()) {
                // Log and ignore if vessel is unknown
                logger.warn("Received position for unknown vessel MMSI: {}. Ignoring position update.", mmsi);
                return;
            }

            // Extract position data from the message
            Double latitude = Double.valueOf(positionData.get("lat").toString());
            Double longitude = Double.valueOf(positionData.get("lon").toString());
            Double speed = Double.valueOf(positionData.get("speed").toString());
            Double course = Double.valueOf(positionData.get("course").toString());
            Integer status = Integer.valueOf(positionData.get("status").toString());
            Double turn = Double.valueOf(positionData.get("turn").toString());
            Integer heading = Integer.valueOf(positionData.get("heading").toString());

            // Convert Unix timestamp to Instant
            Long timestampLong = Long.valueOf(positionData.get("timestamp").toString());
            Instant timestamp = Instant.ofEpochSecond(timestampLong);

            Vessel vessel = existingVessel.get();

            // Create new VesselPosition entity with all the extracted data as fields
            VesselPosition position = new VesselPosition(
                    vessel,
                    latitude,
                    longitude,
                    speed,
                    course,
                    status,
                    turn,
                    heading,
                    timestamp
            );

            // Add VesselPosition to Vessel entity
            vessel.addPosition(position);

            // Handle position history cleanup
            Optional<VesselPosition[]> history = vesselPositionRepository.find2LatestByVesselMmsi(mmsi);
            if (history.isPresent()) {
                VesselPosition[] historyPositions = history.get();
                if (historyPositions.length > 2) {
                    VesselPosition latest = historyPositions[0];
                    VesselPosition prev = historyPositions[1];
                    Long timeStampLongLatest = Long.valueOf(latest.getTimestamp().toString());
                    Long timeStampLongPrev = Long.valueOf(prev.getTimestamp().toString());

                    Instant timestampLatest = Instant.ofEpochSecond(timeStampLongLatest);
                    Instant timestampPrev = Instant.ofEpochSecond(timeStampLongPrev);

                    // If the timestamp we just received and the timestampPrev differ by less than 10 minutes,
                    // overwrite timestampPrev with the new one.
                    if (timestamp.isBefore(timestampPrev.plusSeconds(600))) {
                        vesselPositionRepository.delete(latest);
                    }
                }
            }

            // Save the new VesselPosition to the repository (database)
            vesselPositionRepository.save(position);

            // Log the update with details
            logger.info("Position updated - Vessel: {} (MMSI: {}), Position: lat={}, lon={}, speed={}, course={}, timestamp={}",
                    vessel.getName(), vessel.getMmsi(), latitude, longitude, speed, course, timestamp);

            // Broadcast the vessel position to Frontend using WebSocketService
            webSocketService.broadcastVesselPosition(message, vessel, position);

        } catch (Exception e) {
            logger.error("Error processing vessel position: {}", e.getMessage(), e);
        }
    }
}


// old code:
// @Service
// public class LocationsConsumer {

//     @Autowired
//     SimpMessagingTemplate template;

//     //@Value("{websocket.topic}")
//     //public String websocketTopic;

//     //@Value("{websocket.destinationPrefix}")
//     //public String websocketDestinationPrefix;

//     private final ObjectMapper objectMapper = new ObjectMapper();


//     @KafkaListener(topics = "${kafka.topic}")
//     public void consume(String message) {
//         try {
//             JsonNode jsonNode = objectMapper.readTree(message);
//             template.convertAndSend("/topic/locations", jsonNode.toPrettyString());
//             System.out.println("Sent message: " + jsonNode.toPrettyString()); //debugging
//         } catch (Exception e) {
//             System.err.println(e.getMessage());
//             e.printStackTrace();
//         }

//     }
// }