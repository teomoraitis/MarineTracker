package com.di.marinetracker.backendspringboot.configurations;

import com.di.marinetracker.backendspringboot.services.WebSocketService;
import com.di.marinetracker.backendspringboot.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple broker for broadcasting
        registry.enableSimpleBroker("/topic", "/queue");

        // Set application destination prefix for client messages
        registry.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

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

    /**
     * Handle WebSocket connection events
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Extract JWT token from headers (if present)
        String jwtToken = headerAccessor.getFirstNativeHeader("Authorization");
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7);
        }

        logger.info("WebSocket connection established: {}", sessionId);

        // Register user session if authenticated
        if (jwtToken != null) {
            webSocketService.registerUserSession(sessionId, jwtToken);
        }
    }

    /**
     * Handle WebSocket disconnection events
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        logger.info("WebSocket connection closed: {}", sessionId);

        // Remove user session
        webSocketService.removeUserSession(sessionId);
    }
}