package com.di.marinetracker.backendspringboot.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WebSocketSender {

    private ConcurrentHashMap<String, ArrayList<WebSocketSession>> sessionsOfUser;
    public HashMap<String, VisibleAreaOfSession> visibleAreaOfSession;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void addSession(String username, WebSocketSession session) {
        ArrayList<WebSocketSession> sessions = sessionsOfUser.get(username);
        if (sessions == null) {
            ArrayList<WebSocketSession> thisOneSession = new ArrayList<>();
            thisOneSession.add(session);
            sessionsOfUser.put(username, thisOneSession);
        }
        else {
            sessions.add(session);
        }
        visibleAreaOfSession.put(session.getId(), new VisibleAreaOfSession());
    }
    public void removeSession(String username, WebSocketSession session) {
        ArrayList<WebSocketSession> sessions = sessionsOfUser.get(username);
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsOfUser.remove(username);
        }
        visibleAreaOfSession.remove(session.getId());
    }
    public void sendToUser(String username, String message) {
        for (WebSocketSession session : sessionsOfUser.get(username)) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                logger.error("failed to send message to {}'s socket {}", username, session.getId());
            }
        }
    }
    public Enumeration<String> getActiveUsers() {
        return sessionsOfUser.keys();
    }
    public void forEachSession(BiConsumer<String, WebSocketSession> lambda) {
        sessionsOfUser.forEach((username, sessions) -> {
            sessions.forEach((session) -> {
                lambda.accept(username, session);
            });
        });
    }
}
