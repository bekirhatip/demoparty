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
