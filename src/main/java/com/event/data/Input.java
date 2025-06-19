package com.event.data;

import com.event.ActionType;

public class Input {
    private ActionType actionType;
    private Integer sequenceId;
    private String channelId;
    private Long clientTimestampOffset;
    private short rotateAngle;

    public ActionType getActionType() {
        return actionType;
    }

    public Integer getSequenceId() {
        return sequenceId;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
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

    public short getRotateAngle() {
        return rotateAngle;
    }

    public void setRotateAngle(short rotateAngle) {
        this.rotateAngle = rotateAngle;
    }
}
