package com.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.netty.channel.Channel;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SmallGameEvent {
    private ActionType actionType;
    private Channel channel;
    private Short rotateAngle;
    private Long timestamp;
    private Float dx;
    private Float dy;
    private Long sequenceId;

    public SmallGameEvent() {
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Short getRotateAngle() {
        return rotateAngle;
    }

    public void setRotateAngle(Short rotateAngle) {
        this.rotateAngle = rotateAngle;
    }

    public Float getDx() {
        return dx;
    }

    public void setDx(Float dx) {
        this.dx = dx;
    }

    public Float getDy() {
        return dy;
    }

    public void setDy(Float dy) {
        this.dy = dy;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
