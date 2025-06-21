package com.di.marinetracker.backendspringboot.configurations;

import com.di.marinetracker.backendspringboot.services.WebSocketService;
import com.di.marinetracker.backendspringboot.utils.JwtHandshakeInterceptor;
import com.di.marinetracker.backendspringboot.utils.JwtPrincipal;
import com.di.marinetracker.backendspringboot.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

// Enables WebSocket message handling, backed by a message broker
@EnableWebSocketMessageBroker
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    // Injects the WebSocketService bean (lazy to avoid circular dependency)
    @Autowired
    @Lazy
    private WebSocketService webSocketService;

    // Injects the JwtHandshakeInterceptor for capturing the JWT from request Cookies
    @Autowired
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

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
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(handshakeHandler())
                .setAllowedOriginPatterns("https://localhost:3000")
                .withSockJS();
    }
    @Bean
    public DefaultHandshakeHandler handshakeHandler() {
        return new DefaultHandshakeHandler() {
            @Override
            protected Principal determineUser(ServerHttpRequest request,
                                              WebSocketHandler wsHandler,
                                              Map<String, Object> attributes) {
                Object jwtPrincipal = null;
                if (request instanceof ServletServerHttpRequest servletRequest) {
                    jwtPrincipal = servletRequest.getServletRequest().getAttribute("principal");
                }
                if (jwtPrincipal instanceof Principal) {
                    return (Principal) jwtPrincipal;
                }
                return super.determineUser(request, wsHandler, attributes);
            }
        };
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Principal principal = headerAccessor.getUser();

        logger.info("WebSocket connection established: {}", sessionId);

        if (principal != null) {
            webSocketService.registerUserSession(sessionId, principal);
        } else {
            logger.warn("No principal found on STOMP connect for session {}", sessionId);
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