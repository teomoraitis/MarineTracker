package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import com.di.marinetracker.backendspringboot.repositories.VesselRepository;
import com.di.marinetracker.backendspringboot.repositories.VesselPositionRepository;
import com.di.marinetracker.backendspringboot.websockets.VisibleAreaOfSession;
import com.di.marinetracker.backendspringboot.websockets.WebSocketSender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;

import static java.lang.Long.parseLong;

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

    @Autowired
    WebSocketSender globalWebSocketSender;

    @Transactional
    @KafkaListener(topics = "${kafka.topic}", groupId = "ships-consumer")
    public void consume(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> positionData = objectMapper.readValue(message, Map.class);
            
            // Convert MMSI to String (it comes as Integer from Kafka)
            String mmsi = String.valueOf(positionData.get("mmsi"));
            
            // Check if vessel exists in our database (it should, via static load of name+mmsi+type)
            Optional<Vessel> existingVessel = vesselRepository.findById(mmsi);
            
            if (!existingVessel.isPresent()) {
                logger.warn("Received position for unknown vessel MMSI: {}. Ignoring position update.", mmsi);
                return;
            }

            // Get position data
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
            Optional<VesselPosition[]> history = vesselPositionRepository.find2LatestByVesselMmsi(mmsi);
            if (history.isPresent()) {
                VesselPosition[] historyPositions = history.get();
                if (historyPositions.length > 2) {
                    VesselPosition latest = historyPositions[0];
                    VesselPosition prev   = historyPositions[1];
                    Long timeStampLongLatest = Long.valueOf(latest.getTimestamp().toString());
                    Long timeStampLongPrev   = Long.valueOf(  prev.getTimestamp().toString());

                    Instant timestampLatest = Instant.ofEpochSecond(timeStampLongLatest);
                    Instant timestampPrev   = Instant.ofEpochSecond(timeStampLongPrev);

                    // If the timestamp we just received and the timestampPrev differ by less than 10 minutes,
                    // overwrite timestampPrev with the new one.
                    if (timestamp.isBefore(timestampPrev.plusSeconds(600))) {
                        vesselPositionRepository.delete(latest);
                    }

                    Enumeration<String> activeUsers = globalWebSocketSender.getActiveUsers();

                    // TODO  zone
                    // Query users who just changed their view on the vessel, ie the vessel went from interesting
                    // to non-interesting or vice versa, in terms of zone or speed.
                    // The query would have the whole activeUsers in it.
                    // The DB will return a list with users and it would be convenient to loop here,
                    // and send empty setShips, empty hideShips, false hideAllShips and only notifications.

                    double longitudePrev = Double.valueOf(prev.getLongitude().toString());
                    double latitudePrev = Double.valueOf(prev.getLatitude().toString());
                    globalWebSocketSender.forEachSession((username, session) -> {
                        try {
                            sendPerSessionUpdates(message, session, longitudePrev, latitudePrev, longitude, latitude);
                        } catch (JsonProcessingException e) {
                            logger.error("can't read previous JSONs, it's probably not the client's fault");
                        }
                    });
                }
            }
            vesselPositionRepository.save(position);
            
            // Log the update with details
            logger.info("Position updated - Vessel: {} (MMSI: {}), Position: lat={}, lon={}, speed={}, course={}, timestamp={}", 
                      vessel.getName(), vessel.getMmsi(), latitude, longitude, speed, course, timestamp);
        } catch (Exception e) {
            logger.error("Error processing vessel position: {}", e.getMessage(), e);
        }
    }

    private void sendPerSessionUpdates(String message, WebSocketSession session, double longitudePrev, double latitudePrev, Double longitude, Double latitude) throws JsonProcessingException {
        JsonNode shipData = objectMapper.readTree(message);
        ArrayNode setShips = objectMapper.createArrayNode();
        ArrayNode hideShips = objectMapper.createArrayNode();
        ArrayNode notifications = objectMapper.createArrayNode();

        VisibleAreaOfSession v = globalWebSocketSender.visibleAreaOfSession.get(session.getId());
        boolean wasWithin = v.isWithin(longitudePrev, latitudePrev);
        boolean isWithin = v.isWithin(longitude, latitude);
        if (wasWithin && isWithin) setShips.add(shipData);
        else if (!wasWithin && isWithin) setShips.add(shipData);
        else if (wasWithin && !isWithin) hideShips.add(shipData);

        JsonNode jsonNode = objectMapper.createObjectNode();
        ((ObjectNode) jsonNode).put("setShips", setShips);
        ((ObjectNode) jsonNode).put("hideShips", hideShips);
        ((ObjectNode) jsonNode).put("hideAllShips", false);
        ((ObjectNode) jsonNode).put("notifications", notifications);

        try {
            session.sendMessage(new TextMessage(jsonNode.asText()));
        } catch (IOException e) {
            logger.error("couldn't send web socket message");
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