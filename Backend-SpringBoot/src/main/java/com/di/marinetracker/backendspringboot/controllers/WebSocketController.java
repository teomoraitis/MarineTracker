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

@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private WebSocketService webSocketService;

    /**
     * Handle filter updates from authenticated users
     */
    @MessageMapping("/filters")
    public void updateFilters(@Payload Map<String, Object> filterData, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String sessionId = headerAccessor.getSessionId();

            @SuppressWarnings("unchecked")
            Set<String> vesselTypes = (Set<String>) filterData.get("vesselTypes");

            logger.info("Received filter update from session {}: {}", sessionId, vesselTypes);

            webSocketService.updateUserFilters(sessionId, vesselTypes);

        } catch (Exception e) {
            logger.error("Error updating filters: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle ping messages to keep connection alive
     */
    @MessageMapping("/ping")
    public void handlePing(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        logger.debug("Received ping from session: {}", sessionId);
        // WebSocket framework will automatically send pong
    }
}