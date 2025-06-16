package com.di.marinetracker.backendspringboot.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

import java.util.Map;

public class SimpleWebSocketHandler implements WebSocketHandler {

    private WebSocketSender sender = null;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ObjectMapper objectMapper = new ObjectMapper();

    SimpleWebSocketHandler(WebSocketSender webSocketSender) {
        this.sender = webSocketSender;
    }

    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = String.valueOf(session.getAttributes().get("username"));
        sender.addSession(username, session);
    }

    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = (String) message.getPayload();
        Map<String, Object> map = objectMapper.readValue(payload, Map.class);
        Object typeAsObject = map.get("type");
        if (typeAsObject == null) {
            logger.error("socket sent message that doesn't have a type field");
            session.sendMessage(new TextMessage("{'type': 'error', 'message': 'missing type field'}"));
        }
        String type = String.valueOf(typeAsObject);
        if (type.equals("visibleAreaUpdate")) {
            VisibleAreaOfSession v = sender.visibleAreaOfSession.get(session.getId());
            try {
                v.top = Double.valueOf(map.get("top").toString());
                v.bottom = Double.valueOf(map.get("bottom").toString());
                v.leftSide = Double.valueOf(map.get("leftSide").toString());
                v.rightSide = Double.valueOf(map.get("rightSide").toString());
            } catch (Exception e) {
                session.sendMessage(new TextMessage("{'type': 'error', 'message': 'supported fields in visible area update: top, bottom, leftSide, rightSide'}"));
            }
        }
        else {
            session.sendMessage(new TextMessage("{'type': 'error', 'message': 'unsupported type. Maybe the only supported type is visibleAreaUpdate'}"));
            logger.error("socket sent message with type {}", type);
        }
    }

    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    }

    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String username = String.valueOf(session.getAttributes().get("username"));
        sender.removeSession(username, session);
    }

    public boolean supportsPartialMessages() {
        return false;
    }
}
