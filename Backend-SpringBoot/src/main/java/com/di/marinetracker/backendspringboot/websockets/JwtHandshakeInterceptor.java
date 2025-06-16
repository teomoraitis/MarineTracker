package com.di.marinetracker.backendspringboot.websockets;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String auth = request.getHeaders().getFirst("Authorization");
        if (auth == null) {
            attributes.put("username", "<anonymous>");
            return true;  // the session doesn't need to belong to a user
        }
        if (!auth.startsWith("Bearer ")) {
            logger.info("there is 'Authorization' but it doesn't start with 'Bearer '");
            return false;
        }
        String jwtPart = auth.substring("Bearer ".length());
        try {
            Map<String, Object> map = objectMapper.readValue(jwtPart, Map.class);

            // TODO: VALIDATE !!!!!!!!!!!!!!!!!!!!!

            logger.warn("JWT ON WEB SOCKETS ARE NOT VALIDATED YET!");

            Object usernameAsObject = map.get("username");
            if (usernameAsObject == null) {
                logger.error("JWT don't seem to have a 'username' field anymore");
                return false;
            }
            String username = String.valueOf(usernameAsObject);
            attributes.put("username", username);

        } catch (IOException e) {
            logger.error("JWT, the string after 'Authorization: Bearer ', couldn't be parsed: {}", auth);
            return false;
        }
        return true;
    }
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
    }
}
