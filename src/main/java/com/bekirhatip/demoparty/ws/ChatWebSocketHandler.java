package com.bekirhatip.demoparty.ws;

import com.bekirhatip.demoparty.ws.dto.IncomingMessage;
import com.bekirhatip.demoparty.ws.model.RateLimitState;
import com.bekirhatip.demoparty.ws.model.UserSession;
import com.bekirhatip.demoparty.ws.model.IpRateLimitState;
import com.bekirhatip.demoparty.ws.interceptor.IpHandshakeInterceptor;
import java.net.InetSocketAddress;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, UserSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roomSessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final Map<String, RateLimitState> rateLimitStates = new ConcurrentHashMap<>();

    private final Map<String, IpRateLimitState> ipRateLimits = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipConnectionCount = new ConcurrentHashMap<>();

    private final Map<String, List<String>> roomMessageHistory = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        rateLimitStates.put(session.getId(), new RateLimitState(System.currentTimeMillis()));

        String ip = getClientIp(session);
        ipConnectionCount.putIfAbsent(ip, 0);

        int count = ipConnectionCount.get(ip);
        if (count >= 5) { // max 5 connection per IP
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Too many connections from your IP"));
            System.out.println("Rejected connection from IP " + ip);
            return;
        }

        ipConnectionCount.put(ip, count + 1);

        System.out.println("Connected: " + session.getId() + " IP: " + ip);
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        IncomingMessage incoming = objectMapper.readValue(message.getPayload(), IncomingMessage.class);

        if (incoming.getType() == null) {
            sendError(session, "type is required");
            return;
        }

        switch (incoming.getType()) {
            case "join" -> handleJoin(session, incoming);
            case "message" -> handleMessage(session, incoming);
            case "ping" -> session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
            default -> sendError(session, "Unknown type: " + incoming.getType());
        }
    }

    private void handleJoin(WebSocketSession session, IncomingMessage incoming) throws Exception {

        if (incoming.getUsername() == null || incoming.getUsername().isBlank()) {
            sendError(session, "username is required");
            return;
        }

        if (incoming.getRoom() == null || incoming.getRoom().isBlank()) {
            sendError(session, "room is required");
            return;
        }

        if (incoming.getIcon() == null || incoming.getIcon().isBlank()) {
            sendError(session, "icon is required");
            return;
        }

        if (incoming.getColor() == null || incoming.getColor().isBlank()) {
            sendError(session, "color is required");
            return;
        }

        String baseUsername = incoming.getUsername().trim();
        String room = incoming.getRoom().trim();
        String icon = incoming.getIcon().trim();
        String color = incoming.getColor().trim();

        Set<String> sessionIdsInRoom = roomSessions.getOrDefault(room, Set.of());

        Set<String> existingUsernames = sessionIdsInRoom.stream()
                .map(userSessions::get)
                .filter(Objects::nonNull)
                .map(UserSession::getUsername)
                .collect(Collectors.toSet());
        
        String finalUsername = baseUsername;
        int counter = 2;

        while (existingUsernames.contains(finalUsername)) {
            finalUsername = baseUsername + " (" + counter + ")";
            counter++;
        }

        userSessions.put(session.getId(), new UserSession(finalUsername, room, icon, color, session.getId()));
        roomSessions.computeIfAbsent(room, r -> ConcurrentHashMap.newKeySet()).add(session.getId());

        session.sendMessage(new TextMessage("""
                {"type":"system","message":"joined room %s", "session_id": "%s", "new_username": "%s"}
                """.formatted(room, session.getId(), finalUsername)));

        broadcastToRoom(room, """
                {"type":"system","message":"%s joined the room"}
                """.formatted(escape(finalUsername)));

        publishUserList(room);
    }

    private void handleMessage(WebSocketSession session, IncomingMessage incoming) throws Exception {

        UserSession user = userSessions.get(session.getId());

        if (!checkIpRateLimit(session)) {
            return;
        }

        if (user == null) {
            sendError(session, "You must join first");
            return;
        }

        if (incoming.getMessage() == null || incoming.getMessage().isBlank()) {
            sendError(session, "message is required");
            return;
        }

        String msg = incoming.getMessage().trim();

        if (msg.length() > 5000) {
            sendError(session, "message too long (max 5000 chars)");
            return;
        }

        RateLimitState rl = rateLimitStates.computeIfAbsent(
                session.getId(),
                k -> new RateLimitState(System.currentTimeMillis())
        );

        long now = System.currentTimeMillis();

        if (rl.getMutedUntilMs() > now) {
            long remaining = (rl.getMutedUntilMs() - now) / 1000;
            sendError(session, "You are muted for " + remaining + " seconds");
            return;
        }

        if (now - rl.getWindowStartMs() > 5000) {
            rl.setWindowStartMs(now);
            rl.setMessageCount(0);
        }

        rl.setMessageCount(rl.getMessageCount() + 1);

        if (rl.getMessageCount() > 10) {
            rl.setMutedUntilMs(now + 10_000);
            sendError(session, "Too many messages. Muted for 10 seconds.");
            return;
        }

        if (rl.getLastMessage() != null && rl.getLastMessage().equalsIgnoreCase(msg)) {
            rl.setRepeatedCount(rl.getRepeatedCount() + 1);
        } else {
            rl.setRepeatedCount(0);
            rl.setLastMessage(msg);
        }

        if (rl.getRepeatedCount() >= 4) {
            rl.setMutedUntilMs(now + 15_000);
            sendError(session, "Spam detected. Muted for 15 seconds.");
            return;
        }

        String payload = """
                {"type":"message","username":"%s","room":"%s", "message":"%s", "icon":"%s", "color":"%s", "session_id":"%s"}
                """.formatted(
                escape(user.getUsername()),
                escape(user.getRoom()),
                escape(msg),
                escape(user.getIcon()),
                escape(user.getColor()),
                escape(user.getSession_id())
        );

        broadcastToRoom(user.getRoom(), payload);

        roomMessageHistory.computeIfAbsent(user.getRoom(), k -> new LinkedList<>());
        List<String> history = roomMessageHistory.get(user.getRoom());
        history.add(payload);
        if (history.size() > 50) history.remove(0);


    }

    private void publishUserList(String room) throws Exception {

        Set<String> sessionIds = roomSessions.get(room);
        if (sessionIds == null) return;

        List<Map<String, String>> users = new LinkedList<>();

        for (String sessionId : sessionIds) {
            UserSession us = userSessions.get(sessionId);
            if (us != null) {
                users.add(Map.of(
                    "username", us.getUsername(),
                    "icon", us.getIcon(),
                    "color", us.getColor(),
                    "session_id", us.getSession_id()
                ));
            }
        }


        String payload = objectMapper.writeValueAsString(Map.of(
                "type", "user_list",
                "room", room,
                "users", users
        ));

        broadcastToRoom(room, payload);
    }

    private void broadcastToRoom(String room, String payload) throws Exception {

        Set<String> sessionIds = roomSessions.get(room);
        if (sessionIds == null) return;

        for (String sessionId : sessionIds) {
            WebSocketSession s = sessions.get(sessionId);
            if (s == null) continue;
            if (!s.isOpen()) continue;

            s.sendMessage(new TextMessage(payload));
        }
    }

    private void sendError(WebSocketSession session, String msg) throws Exception {
        session.sendMessage(new TextMessage("""
                {"type":"error","message":"%s"}
                """.formatted(escape(msg))));
    }

    private String escape(String text) {
        return text.replace("\"", "\\\"");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        sessions.remove(session.getId());
        rateLimitStates.remove(session.getId());

        String ip = getClientIp(session);
        ipConnectionCount.computeIfPresent(ip, (k, v) -> v > 0 ? v - 1 : 0);

        UserSession user = userSessions.remove(session.getId());

        if (user != null) {
            String room = user.getRoom();
            String username = user.getUsername();
            String icon = user.getIcon();
            String color = user.getColor();

            Set<String> set = roomSessions.get(room);
            if (set != null) {
                set.remove(session.getId());

                if (set.isEmpty()) {
                    roomSessions.remove(room);
                }
            }

            try {
                broadcastToRoom(room, """
                        {"type":"system","message":"%s left the room"}
                        """.formatted(escape(username)));

                publishUserList(room);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Disconnected: " + session.getId());
    }

    private String getClientIp(WebSocketSession session) {

        Object ipObj = session.getAttributes().get("clientIp");

        if (ipObj != null) {
            return ipObj.toString();
        }

        return "unknown";
    }


    private boolean checkIpRateLimit(WebSocketSession session) throws Exception {

        String ip = getClientIp(session);
        long now = System.currentTimeMillis();

        IpRateLimitState state = ipRateLimits.computeIfAbsent(ip, k -> new IpRateLimitState(now));

        // is muted?
        if (state.getMutedUntilMs() > now) {
            long remaining = (state.getMutedUntilMs() - now) / 1000;
            sendError(session, "IP muted for " + remaining + " seconds");
            return false;
        }

        // 3-second window
        if (now - state.getWindowStartMs() > 3000) {
            state.setWindowStartMs(now);
            state.setMessageCount(0);
        }

        state.setMessageCount(state.getMessageCount() + 1);

        // Maximum 25 messages in 3 seconds (IP limit)
        if (state.getMessageCount() > 25) {
            state.setMutedUntilMs(now + 20_000); // 20 sec mute
            sendError(session, "Too many requests from IP. Muted for 20 seconds.");
            return false;
        }

        return true;
    }

}
