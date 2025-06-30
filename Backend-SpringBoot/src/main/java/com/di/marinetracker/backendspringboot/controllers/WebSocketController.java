package com.di.marinetracker.backendspringboot.controllers;

import com.di.marinetracker.backendspringboot.services.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Set;

// Marks this class as a WebSocket controller
@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    // Injects the WebSocketService
    @Autowired
    private WebSocketService webSocketService;

    // Handles filter updates sent by authenticated users over WebSocket
    // accessible to the client as /app/filters
    @MessageMapping("/filters")
    public void updateFilters(@Payload Map<String, Object> filterData, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = headerAccessor.getSessionId();

            // Handle vessel type filters
            @SuppressWarnings("unchecked")
            Set<String> vesselTypes = (Set<String>) filterData.get("vesselTypes");

            if (vesselTypes != null) {
                logger.info("Received vessel type filter update from session {}: {}", sessionId, vesselTypes);
                webSocketService.updateUserFilters(sessionId, vesselTypes);
            }

            // Handle fleet filter
            Boolean showOnlyFleet = (Boolean) filterData.get("showOnlyFleet");
            if (showOnlyFleet != null) {
                logger.info("Received fleet filter update from session {}: {}", sessionId, showOnlyFleet);
                webSocketService.updateFleetFilter(sessionId, showOnlyFleet);
            }

        } catch (Exception e) {
            logger.error("Error updating filters: {}", e.getMessage(), e);
        }
    }



    // Handles ping messages to keep the WebSocket connection alive
    //accessible to the client as /app/ping
    @MessageMapping("/ping")
    public void handlePing(SimpMessageHeaderAccessor headerAccessor) {
        // Get the WebSocket session ID
        String sessionId = headerAccessor.getSessionId();
        logger.debug("Received ping from session: {}", sessionId);

        // WebSocket framework will automatically send pong
    }
}