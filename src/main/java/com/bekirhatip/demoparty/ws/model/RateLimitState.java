// This class represents the state of rate limiting for a specific user session. It tracks the start time of the current rate limit window, the count of messages sent within that window, the time until which the user is muted if they have exceeded the allowed message count, and information about repeated messages. This information is used to enforce rate limits on incoming messages from clients to prevent abuse and ensure fair usage of the chat service.

package com.bekirhatip.demoparty.ws.model;

public class RateLimitState {

    private long windowStartMs;
    private int messageCount;

    private long mutedUntilMs;

    private String lastMessage;
    private int repeatedCount;

    public RateLimitState(long now) {
        this.windowStartMs = now;
        this.messageCount = 0;
        this.mutedUntilMs = 0;
        this.lastMessage = null;
        this.repeatedCount = 0;
    }

    public long getWindowStartMs() {
        return windowStartMs;
    }

    public void setWindowStartMs(long windowStartMs) {
        this.windowStartMs = windowStartMs;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public long getMutedUntilMs() {
        return mutedUntilMs;
    }

    public void setMutedUntilMs(long mutedUntilMs) {
        this.mutedUntilMs = mutedUntilMs;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getRepeatedCount() {
        return repeatedCount;
    }

    public void setRepeatedCount(int repeatedCount) {
        this.repeatedCount = repeatedCount;
    }
}
