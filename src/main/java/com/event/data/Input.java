package com.event.data;

import com.event.ActionType;

public class Input {
    private ActionType actionType;
    private Long timestamp;
    private Short rotateAngle;
    private Float dx;
    private Float dy;
    private Long sequenceId;
    private String channelId;

    public Input() {}

    public Input(ActionType actionType, Long timestamp, Short rotateAngle, Float dx, Float dy, Long sequenceId) {
        this.actionType = actionType;
        this.timestamp = timestamp;
        this.rotateAngle = rotateAngle;
        this.dx = dx;
        this.dy = dy;
        this.sequenceId = sequenceId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Short getRotateAngle() {
        return rotateAngle;
    }

    public Float getDx() {
        return dx;
    }

    public Float getDy() {
        return dy;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setRotateAngle(Short rotateAngle) {
        this.rotateAngle = rotateAngle;
    }

    public void setDx(Float dx) {
        this.dx = dx;
    }

    public void setDy(Float dy) {
        this.dy = dy;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
