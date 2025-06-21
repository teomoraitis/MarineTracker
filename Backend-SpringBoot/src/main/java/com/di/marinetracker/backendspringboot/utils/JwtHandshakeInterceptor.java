package com.di.marinetracker.backendspringboot.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Arrays;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtUtils jwtUtils;

    public JwtHandshakeInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) throws Exception {

        // Extract JWT from cookies
        if (request instanceof org.springframework.http.server.ServletServerHttpRequest servletRequest) {
            var cookies = servletRequest.getServletRequest().getCookies();
            if (cookies != null) {
                Arrays.stream(cookies)
                        .filter(c -> c.getName().equals("jwt"))
                        .findFirst()
                        .ifPresent(jwtCookie -> {
                            String jwt = jwtCookie.getValue();
                            System.out.println(jwt);
                            String userName = jwtUtils.getUserNameFromJwtToken(jwt);
                            // Store in attributes as fallback
                            attributes.put("jwtToken", jwt);

                            JwtPrincipal jwtPrincipal = new JwtPrincipal(jwt, userName);
                            attributes.put("principal", jwtPrincipal);
                            // Also set the user principal
                            servletRequest.getServletRequest().setAttribute("principal", jwtPrincipal);
                        });
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // nothing to do here
    }
}
