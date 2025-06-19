package com;

import io.netty.channel.Channel;
import org.dyn4j.dynamics.Body;


public class Player {
    private Channel channel;
    private String name;
    private Body body;
    private int lastProcessedSequenceId;
    private short rotateAngle;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
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

    public short getRotateAngle() {
        return rotateAngle;
    }

    public void setRotateAngle(short rotateAngle) {
        this.rotateAngle = rotateAngle;
    }
}
