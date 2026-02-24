// This class is a WebSocket handshake interceptor that extracts the client's IP address from the incoming HTTP request during the WebSocket handshake process. It checks for common headers like "X-Forwarded-For" and "X-Real-IP" to determine the client's real IP address, especially when the application is behind a reverse proxy. The extracted IP address is then stored in the WebSocket session attributes for later use, such as rate limiting or logging.

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
