package com.di.marinetracker.backendspringboot.configurations;

import com.di.marinetracker.backendspringboot.services.WebSocketService;
import com.di.marinetracker.backendspringboot.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

// Enables WebSocket message handling, backed by a message broker
@EnableWebSocketMessageBroker
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    // Injects the WebSocketService bean (lazy to avoid circular dependency)
    @Autowired
    @Lazy
    private WebSocketService webSocketService;

    // Injects the JwtUtils bean for JWT operations
    @Autowired
    private JwtUtils jwtUtils;

    // Configures the message broker for handling messages between clients and server
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enables a simple in-memory broker for broadcasting messages to /topic and /queue destinations
        registry.enableSimpleBroker("/topic", "/queue");

        // Set application destination prefix for client messages
        registry.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

    // Registers STOMP endpoints for WebSocket connections
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Public endpoint for guests
        registry.addEndpoint("/ws/guest")
                .setAllowedOriginPatterns("https://localhost:3000")
                .withSockJS();

        // Authenticated endpoint for logged-in users
        registry.addEndpoint("/ws/auth")
                .setAllowedOriginPatterns("https://localhost:3000")
                .withSockJS();
    }

    // Handles WebSocket connection events
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // Extract the session ID from the STOMP headers
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Extract JWT token from the Authorization header, if present
        String jwtToken = headerAccessor.getFirstNativeHeader("Authorization");
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7);
        }

        logger.info("WebSocket connection established: {}", sessionId);

        // Register the user session if a JWT token is provided
        if (jwtToken != null) {
            webSocketService.registerUserSession(sessionId, jwtToken);
        }
    }

    // Handles WebSocket disconnection events
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // Extract the session ID from the STOMP headers
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        logger.info("WebSocket connection closed: {}", sessionId);

        // Remove the user session from the service
        webSocketService.removeUserSession(sessionId);
    }
}