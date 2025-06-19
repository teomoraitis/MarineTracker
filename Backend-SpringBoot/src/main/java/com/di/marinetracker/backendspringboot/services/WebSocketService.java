package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.ZoneOfInterest;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import com.di.marinetracker.backendspringboot.utils.JwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    // Store active user sessions and their filters
    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();

    // Store vessel data cache for filtering
    private final Map<String, JsonNode> vesselDataCache = new ConcurrentHashMap<>();

    /**
     * Broadcast vessel position to all channels (public and authenticated users)
     */
    public void broadcastVesselPosition(String vesselMessage, Vessel vessel) {
        try {
            JsonNode vesselData = objectMapper.readTree(vesselMessage);
            String mmsi = vessel.getMmsi();

            // Cache the vessel data
            vesselDataCache.put(mmsi, vesselData);

            // Broadcast to public guest endpoint
            broadcastToGuests(vesselData);

            // Broadcast to authenticated users with filtering
            broadcastToAuthenticatedUsers(vesselData, vessel);

        } catch (Exception e) {
            logger.error("Error broadcasting vessel position: {}", e.getMessage(), e);
        }
    }

    /**
     * Broadcast to public guest endpoint - all vessels visible
     */
    private void broadcastToGuests(JsonNode vesselData) {
        try {
            JsonNode guestMessage = createWebSocketMessage(
                    Arrays.asList(vesselData),
                    Collections.emptyList(),
                    false,
                    Collections.emptyList()
            );

            messagingTemplate.convertAndSend("/topic/guest", guestMessage.toString());
            logger.debug("Broadcasted to guest topic: vessel {}", vesselData.get("mmsi"));

        } catch (Exception e) {
            logger.error("Error broadcasting to guests: {}", e.getMessage(), e);
        }
    }

    /**
     * Broadcast to authenticated users with personalized filtering
     */
    private void broadcastToAuthenticatedUsers(JsonNode vesselData, Vessel vessel) {
        activeSessions.forEach((sessionId, userSession) -> {
            try {
                if (shouldSendToUser(vessel, userSession)) {
                    List<String> notifications = generateNotifications(vessel, userSession);

                    JsonNode userMessage = createWebSocketMessage(
                            Arrays.asList(vesselData),
                            Collections.emptyList(),
                            false,
                            notifications
                    );

                    messagingTemplate.convertAndSendToUser(
                            userSession.getUserId(),
                            "/queue/vessels",
                            userMessage.toString()
                    );

                    logger.debug("Sent vessel {} to user {}", vessel.getMmsi(), userSession.getUserId());
                }
            } catch (Exception e) {
                logger.error("Error sending to user {}: {}", userSession.getUserId(), e.getMessage(), e);
            }
        });
    }

    /**
     * Determine if a vessel should be sent to a specific user based on their filters
     */
    private boolean shouldSendToUser(Vessel vessel, UserSession userSession) {
        // Always show vessels in user's fleet
        if (userSession.getFleetMmsis().contains(vessel.getMmsi())) {
            return true;
        }

        // Apply vessel type filters
        if (!userSession.getVesselTypeFilters().isEmpty() &&
                !userSession.getVesselTypeFilters().contains(vessel.getType())) {
            return false;
        }

        // Apply zone of interest filter
        if (userSession.getZoneOfInterest() != null) {
            return userSession.getZoneOfInterest().matchesConditions(vessel);
        }

        // If no specific filters, show all vessels
        return true;
    }

    /**
     * Generate notifications for zone of interest alerts
     */
    private List<String> generateNotifications(Vessel vessel, UserSession userSession) {
        List<String> notifications = new ArrayList<>();

        if (userSession.getZoneOfInterest() != null &&
                userSession.getZoneOfInterest().matchesConditions(vessel)) {

            notifications.add(String.format(
                    "Vessel %s (%s) entered your zone of interest",
                    vessel.getName(),
                    vessel.getMmsi()
            ));
        }

        return notifications;
    }

    /**
     * Register a new user session when they connect
     */
    public void registerUserSession(String sessionId, String jwtToken) {
        try {
            if (jwtToken != null && jwtUtils.validateJwtToken(jwtToken)) {
                String userId = jwtUtils.getUserNameFromJwtToken(jwtToken);
                Optional<User> userOpt = userRepository.findById(userId);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    UserSession session = new UserSession(
                            userId,
                            sessionId,
                            user.getFleet().stream().map(Vessel::getMmsi).toList(),
                            new HashSet<>(), // Default: no vessel type filters
                            user.getZoneOfInterest()
                    );

                    activeSessions.put(sessionId, session);
                    logger.info("Registered user session: {} for user: {}", sessionId, userId);

                    // Send initial vessel data to the newly connected user
                    sendInitialDataToUser(session);
                }
            }
        } catch (Exception e) {
            logger.error("Error registering user session: {}", e.getMessage(), e);
        }
    }

    /**
     * Remove user session when they disconnect
     */
    public void removeUserSession(String sessionId) {
        UserSession removed = activeSessions.remove(sessionId);
        if (removed != null) {
            logger.info("Removed user session: {} for user: {}", sessionId, removed.getUserId());
        }
    }

    /**
     * Update user's vessel type filters
     */
    public void updateUserFilters(String sessionId, Set<String> vesselTypes) {
        UserSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.setVesselTypeFilters(vesselTypes);
            logger.info("Updated filters for session {}: {}", sessionId, vesselTypes);

            // Send updated vessel data based on new filters
            sendFilteredDataToUser(session);
        }
    }

    /**
     * Send initial vessel data when user first connects
     */
    private void sendInitialDataToUser(UserSession userSession) {
        try {
            List<JsonNode> visibleVessels = new ArrayList<>();

            vesselDataCache.forEach((mmsi, vesselData) -> {
                // This is a simplified check - in production you'd want to fetch full vessel data
                if (userSession.getFleetMmsis().contains(mmsi)) {
                    visibleVessels.add(vesselData);
                }
            });

            if (!visibleVessels.isEmpty()) {
                JsonNode initialMessage = createWebSocketMessage(
                        visibleVessels,
                        Collections.emptyList(),
                        false,
                        Arrays.asList("Connected to vessel tracking")
                );

                messagingTemplate.convertAndSendToUser(
                        userSession.getUserId(),
                        "/queue/vessels",
                        initialMessage.toString()
                );
            }

        } catch (Exception e) {
            logger.error("Error sending initial data to user: {}", e.getMessage(), e);
        }
    }

    /**
     * Send filtered data when user updates their filters
     */
    private void sendFilteredDataToUser(UserSession userSession) {
        // Implementation would be similar to sendInitialDataToUser
        // but would apply the current filters
        logger.info("Sending filtered data to user: {}", userSession.getUserId());
    }

    /**
     * Create standardized WebSocket message format
     */
    private JsonNode createWebSocketMessage(List<JsonNode> setShips, List<String> hideShips,
                                            boolean hideAllShips, List<String> notifications) {
        ObjectNode message = objectMapper.createObjectNode();

        ArrayNode setShipsArray = objectMapper.createArrayNode();
        setShips.forEach(setShipsArray::add);

        ArrayNode hideShipsArray = objectMapper.createArrayNode();
        hideShips.forEach(hideShipsArray::add);

        ArrayNode notificationsArray = objectMapper.createArrayNode();
        notifications.forEach(notificationsArray::add);

        message.set("setShips", setShipsArray);
        message.set("hideShips", hideShipsArray);
        message.put("hideAllShips", hideAllShips);
        message.set("notifications", notificationsArray);
        message.put("timestamp", System.currentTimeMillis());

        return message;
    }

    /**
     * Get count of active sessions (for monitoring)
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Inner class to represent a user session
     */
    private static class UserSession {
        private final String userId;
        private final String sessionId;
        private final List<String> fleetMmsis;
        private Set<String> vesselTypeFilters;
        private final ZoneOfInterest zoneOfInterest;

        public UserSession(String userId, String sessionId, List<String> fleetMmsis,
                           Set<String> vesselTypeFilters, ZoneOfInterest zoneOfInterest) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.fleetMmsis = fleetMmsis;
            this.vesselTypeFilters = vesselTypeFilters;
            this.zoneOfInterest = zoneOfInterest;
        }

        // Getters and setters
        public String getUserId() { return userId; }
        public String getSessionId() { return sessionId; }
        public List<String> getFleetMmsis() { return fleetMmsis; }
        public Set<String> getVesselTypeFilters() { return vesselTypeFilters; }
        public void setVesselTypeFilters(Set<String> vesselTypeFilters) {
            this.vesselTypeFilters = vesselTypeFilters;
        }
        public ZoneOfInterest getZoneOfInterest() { return zoneOfInterest; }
    }
}