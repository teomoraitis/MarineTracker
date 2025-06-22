package com.di.marinetracker.backendspringboot.services;

import com.di.marinetracker.backendspringboot.controllers.VesselCacheController;
import com.di.marinetracker.backendspringboot.entities.User;
import com.di.marinetracker.backendspringboot.entities.Vessel;
import com.di.marinetracker.backendspringboot.entities.VesselPosition;
import com.di.marinetracker.backendspringboot.entities.ZoneOfInterest;
import com.di.marinetracker.backendspringboot.repositories.UserRepository;
import com.di.marinetracker.backendspringboot.utils.JwtPrincipal;
import com.di.marinetracker.backendspringboot.utils.JwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Service that handles WebSocket communication with Frontend clients (guests or authenticated users)
@Service
public class WebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // For sending WebSocket messages
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Access user data
    @Autowired
    private UserRepository userRepository;

    // JWT utility for authentication
    @Autowired
    private JwtUtils jwtUtils;

    // Map to store active user sessions and their filters
    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();

    // Cache for vessel data to support filtering and quick access
    @Autowired
    private VesselCacheController vesselDataCache;

    // Broadcast vessel position to all channels (public-guest and authenticated users)
    public void broadcastVesselPosition(String vesselMessage, Vessel vessel, VesselPosition  vesselPosition) {
        try {
            // Parse vessel message
            JsonNode vesselData = objectMapper.readTree(vesselMessage);
            String mmsi = vessel.getMmsi();

            // Cache the vessel data
            vesselDataCache.put(mmsi, vesselData, vesselPosition);

            // Broadcast to public guest endpoint by calling broadcastToGuests()
            broadcastToGuests(vesselData);

            // Broadcast to authenticated users with filtering by calling broadcastToAuthenticatedUsers()
            broadcastToAuthenticatedUsers(vesselData, vessel);

        } catch (Exception e) {
            logger.error("Error broadcasting vessel position: {}", e.getMessage(), e);
        }
    }

    // Broadcast to public guest endpoint - all vessels visible
    private void broadcastToGuests(JsonNode vesselData) {
        try {
            // Create a message for guests with the vessel data
            JsonNode guestMessage = createWebSocketMessage(
                    Arrays.asList(vesselData),
                    Collections.emptyList(),
                    false,
                    Collections.emptyList()
            );

            // Send to guest topic
            messagingTemplate.convertAndSend("/topic/guest", guestMessage.toString());
            logger.info("Broadcasted to guest topic: vessel {}", vesselData.get("mmsi"));

        } catch (Exception e) {
            logger.error("Error broadcasting to guests: {}", e.getMessage(), e);
        }
    }

    // Broadcast to authenticated users with personalized filtering
    private void broadcastToAuthenticatedUsers(JsonNode vesselData, Vessel vessel) {
        activeSessions.forEach((sessionId, userSession) -> {
            try {
                // Check if user should receive this vessel by calling shouldSendToUser()
                if (shouldSendToUser(vessel, userSession)) {
                    // Generate notifications for zone of interest alerts by calling generateNotifications()
                    List<String> notifications = generateNotifications(vessel, userSession);

                    // Create the vessel data message + notifications
                    JsonNode userMessage = createWebSocketMessage(
                            Arrays.asList(vesselData),
                            Collections.emptyList(),
                            false,
                            notifications
                    );

                    // Send the message to the user's specific queue
                    messagingTemplate.convertAndSendToUser(
                            userSession.getUserId(),
                            "/queue/vessels",
                            userMessage.toString()
                    );

                    logger.info("Sent vessel {} to user {}", vessel.getMmsi(), userSession.getUserId());
                }
            } catch (Exception e) {
                logger.error("Error sending to user {}: {}", userSession.getUserId(), e.getMessage(), e);
            }
        });
    }

    // Determine if a vessel should be sent to a specific user based on their filters
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

    // Generate notifications for zone of interest alerts
    // TODO: Maybe this will change to a separate Notification Entity + Repository + Service etc...
    private List<String> generateNotifications(Vessel vessel, UserSession userSession) {
        List<String> notifications = new ArrayList<>();

        // Check if vessel matches user's zone of interest conditions
        if (userSession.getZoneOfInterest() != null &&
                userSession.getZoneOfInterest().matchesConditions(vessel)) {

            // Add notification for entering zone of interest
            notifications.add(String.format(
                    "Vessel %s (%s) entered your zone of interest",
                    vessel.getName(),
                    vessel.getMmsi()
            ));
        }

        return notifications;
    }

    // Register a new user session when they connect
    @Transactional
    public void registerUserSession(String sessionId, Principal principal) {
        try {
            if (principal == null) {
                logger.warn("No principal found for session {}", sessionId);
                return;
            }

            String userId = principal.getName();

            // Optional: if you want to re-validate the JWT for extra security:
            if (principal instanceof JwtPrincipal jwtPrincipal) {
                String jwt = jwtPrincipal.getJwt();
                if (!jwtUtils.validateJwtToken(jwt)) {
                    logger.warn("Invalid JWT for session {}", sessionId);
                    return;
                }
            }

            Optional<User> userOpt = userRepository.findByUserNameWithFleet(userId);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                UserSession session = new UserSession(
                        userId,
                        sessionId,
                        user.getFleet().stream().map(Vessel::getMmsi).toList(),
                        new HashSet<>(),
                        user.getZoneOfInterest()
                );

                activeSessions.put(sessionId, session);
                logger.info("Registered user session: {} for user: {}", sessionId, userId);

                sendInitialDataToUser(session);
            } else {
                logger.warn("User not found for session {} and userId {}", sessionId, userId);
            }

        } catch (Exception e) {
            logger.error("Error registering user session: {}", e.getMessage(), e);
        }
    }

    // Remove user session when they disconnect
    public void removeUserSession(String sessionId) {
        UserSession removed = activeSessions.remove(sessionId);
        if (removed != null) {
            logger.info("Removed user session: {} for user: {}", sessionId, removed.getUserId());
        }
    }

    // Update user's vessel type filters
    public void updateUserFilters(String sessionId, Set<String> vesselTypes) {
        UserSession session = activeSessions.get(sessionId);
        if (session != null) {
            // Update filters
            session.setVesselTypeFilters(vesselTypes);
            logger.info("Updated filters for session {}: {}", sessionId, vesselTypes);

            // Send updated vessel data based on new filters
            sendFilteredDataToUser(session);
        }
    }

    // Send initial vessel data when user first connects
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

    // Send filtered data when user updates their filters
    private void sendFilteredDataToUser(UserSession userSession) {
        // TODO: Implementation would be similar to sendInitialDataToUser
        // but would apply the current filters
        logger.info("Sending filtered data to user: {}", userSession.getUserId());
    }

    // Create standardized WebSocket message format
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

    // Get count of active sessions (for monitoring, not used for now)
//    public int getActiveSessionCount() {
//        return activeSessions.size();
//    }

    // Inner class to represent a user session
    private static class UserSession {
        private final String userId; // User identifier
        private final String sessionId; // WebSocket session ID
        private final List<String> fleetMmsis; // List of vessel MMSIs in user's fleet
        private Set<String> vesselTypeFilters; // Vessel type filters
        private final ZoneOfInterest zoneOfInterest; // User's zone of interest

        // Constructor
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