package com.event.data;

import com.event.ActionType;

public class Input {
    private ActionType actionType;
    private Float dx;
    private Float dy;
    private Long sequenceId;
    private String channelId;
    private Long clientTimestampOffset;

    public Input() {}

    public Input(ActionType actionType, Float dx, Float dy, Long sequenceId) {
        this.actionType = actionType;
        this.dx = dx;
        this.dy = dy;
        this.sequenceId = sequenceId;
    }

    public ActionType getActionType() {
        return actionType;
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

    public Long getClientTimestampOffset() {
        return clientTimestampOffset;
    }

    public void setClientTimestampOffset(Long clientTimestampOffset) {
        this.clientTimestampOffset = clientTimestampOffset;
    }
}
