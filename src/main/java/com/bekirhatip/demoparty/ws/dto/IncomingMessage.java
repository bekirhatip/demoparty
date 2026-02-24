// This class represents the structure of incoming messages from clients in the chat application. It contains fields for the message type, username, chat room, user icon, message color, and the message content itself. This class is used to deserialize JSON messages received from clients into Java objects that can be processed by the WebSocket handler.

package com.bekirhatip.demoparty.ws.dto;

public class IncomingMessage {
    private String type;
    private String username;
    private String room;
    private String icon;
    private String color;
    private String message;

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getRoom() {
        return room;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public String getMessage() {
        return message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
    public void setColor(String color) {
        this.color = color;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
