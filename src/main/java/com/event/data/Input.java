package com.event.data;

import com.event.ActionType;

public class Input {
    private ActionType actionType;
    private Double dx;
    private Double dy;
    private Integer sequenceId;
    private String channelId;
    private Long clientTimestampOffset;
    private float deltaTime;

    public Input() {
    }

    public Input(ActionType actionType, Double dx, Double dy, Integer sequenceId) {
        this.actionType = actionType;
        this.dx = dx;
        this.dy = dy;
        this.sequenceId = sequenceId;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public Double getDx() {
        return dx;
    }

    public Double getDy() {
        return dy;
    }

    public Integer getSequenceId() {
        return sequenceId;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public void setDx(Double dx) {
        this.dx = dx;
    }

    public void setDy(Double dy) {
        this.dy = dy;
    }

    public void setSequenceId(Integer sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Long getClientTimestampOffset() {
        return clientTimestampOffset;
    }

    public void setClientTimestampOffset(Long clientTimestampOffset) {
        this.clientTimestampOffset = clientTimestampOffset;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(float deltaTime) {
        this.deltaTime = deltaTime;
    }

    @Override
    public String toString() {
        return "Input{" +
                "actionType=" + actionType +
                ", dx=" + dx +
                ", dy=" + dy +
                ", sequenceId=" + sequenceId +
                ", channelId='" + channelId + '\'' +
                ", clientTimestampOffset=" + clientTimestampOffset +
                ", deltaTime=" + deltaTime +
                '}';
    }
}
