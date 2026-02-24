package com.bekirhatip.demoparty.ws.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public class IpHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            String forwardedFor = httpRequest.getHeader("X-Forwarded-For");
            String realIp = httpRequest.getHeader("X-Real-IP");

            String clientIp = null;

            if (forwardedFor != null && !forwardedFor.isBlank()) {
                clientIp = forwardedFor.split(",")[0].trim();
            } else if (realIp != null && !realIp.isBlank()) {
                clientIp = realIp.trim();
            } else {
                clientIp = httpRequest.getRemoteAddr();
            }

            attributes.put("clientIp", clientIp);
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
        // no-op
    }
}
