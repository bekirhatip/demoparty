// This class represents a user session in the chat application. It contains information about the user's username, the chat room they are in, their chosen icon and color for messages, and their unique session ID. This information is used to manage user sessions and personalize the chat experience for each user.

package com.bekirhatip.demoparty.ws.model;

public class UserSession {
    private final String username;
    private final String room;
    private final String icon;
    private final String color;
    private final String session_id;

    public UserSession(String username, String room, String icon, String color, String session_id) {
        this.username = username;
        this.room = room;
        this.icon = icon;
        this.color = color;
        this.session_id = session_id;
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

    public String getSession_id() {
        return session_id;
    }
}
