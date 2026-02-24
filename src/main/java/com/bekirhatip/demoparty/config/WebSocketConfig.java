// This class is responsible for configuring WebSocket support in the Spring Boot application. It registers the ChatWebSocketHandler to handle WebSocket connections at the specified endpoint ("/ws/chat") and adds an interceptor (IpHandshakeInterceptor) to extract the client's IP address during the handshake process. It also allows cross-origin requests from any origin for WebSocket connections.

package com.bekirhatip.demoparty.config;

import com.bekirhatip.demoparty.ws.interceptor.IpHandshakeInterceptor;
import com.bekirhatip.demoparty.ws.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(new IpHandshakeInterceptor())
                .setAllowedOrigins("*");
    }

}
