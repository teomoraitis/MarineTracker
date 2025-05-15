package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import com.di.marinetracker.backendspringboot.repositories.VesselRepository;
import com.di.marinetracker.backendspringboot.repositories.VesselPositionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class VesselPositionsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(VesselPositionsConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final VesselRepository vesselRepository;
    private final VesselPositionRepository vesselPositionRepository;

    public VesselPositionsConsumer(VesselRepository vesselRepository,
                                 VesselPositionRepository vesselPositionRepository) {
        this.vesselRepository = vesselRepository;
        this.vesselPositionRepository = vesselPositionRepository;
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic}", groupId = "ships-consumer")
    public void consume(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> positionData = objectMapper.readValue(message, Map.class);
            
            // Convert MMSI to String (it comes as Integer from Kafka)
            String mmsi = String.valueOf(positionData.get("mmsi"));
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
            
            String type = positionData.get("type") != null ? String.valueOf(positionData.get("type")) : "Unknown";
            
            // Get or create vessel
            Vessel vessel = vesselRepository.findById(mmsi)
                    .orElseGet(() -> {
                        String vesselName = positionData.get("name") != null ? 
                            String.valueOf(positionData.get("name")) : 
                            "Unknown Vessel " + mmsi;
                        Vessel newVessel = new Vessel(mmsi, type, vesselName);
                        return vesselRepository.save(newVessel);
                    });

            // Create new position with all fields
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

            // Save position and update vessel
            vessel.addPosition(position);
            vesselPositionRepository.save(position);
            
            logger.info("Processed position update for vessel MMSI: {}", mmsi);
        } catch (Exception e) {
            logger.error("Error processing vessel position: {}", e.getMessage(), e);
        }
    }
}

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