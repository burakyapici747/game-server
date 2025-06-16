package com;

import org.dyn4j.dynamics.Body;

public class Player {
    private String channelId;
    private String name;
    private Body body;
    private int lastProcessedSequenceId;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public int getLastProcessedSequenceId() {
        return lastProcessedSequenceId;
    }

    public void setLastProcessedSequenceId(int lastProcessedSequenceId) {
        this.lastProcessedSequenceId = lastProcessedSequenceId;
    }
}
