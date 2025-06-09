package com.event.data;

import com.event.ActionType;

public class Input {
    private ActionType actionType;
    private Double dx;
    private Double dy;
    private Long sequenceId;
    private String channelId;
    private Long clientTimestampOffset;

    public Input() {}

    public Input(ActionType actionType, Double dx, Double dy, Long sequenceId) {
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

    public Long getSequenceId() {
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

    public void setSequenceId(Long sequenceId) {
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
}
