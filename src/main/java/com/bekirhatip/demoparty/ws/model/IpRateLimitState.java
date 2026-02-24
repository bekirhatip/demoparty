// This class represents the state of rate limiting for a specific IP address. It tracks the start time of the current rate limit window, the count of messages sent within that window, and the time until which the IP is muted if it has exceeded the allowed message count. This information is used to enforce rate limits on incoming messages from clients to prevent abuse and ensure fair usage of the chat service.

package com.bekirhatip.demoparty.ws.model;

public class IpRateLimitState {

    private long windowStartMs;
    private int messageCount;

    private long mutedUntilMs;

    public IpRateLimitState(long now) {
        this.windowStartMs = now;
        this.messageCount = 0;
        this.mutedUntilMs = 0;
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
}
