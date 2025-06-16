package com.di.marinetracker.backendspringboot.websockets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

    private WebSocketSender globalWebSocketSender = new WebSocketSender();

    @Bean
    public WebSocketSender webSocketSender() {
        return globalWebSocketSender;
    }

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        SimpleWebSocketHandler handler = new SimpleWebSocketHandler(globalWebSocketSender);
        JwtHandshakeInterceptor interceptor = new JwtHandshakeInterceptor();
        registry.addHandler(handler, "/app/ws").addInterceptors(interceptor);
        // TODO: allowed origins
    }
}
