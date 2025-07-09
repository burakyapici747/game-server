package com.event.data;

import com.event.ActionType;

public class Input {
    private ActionType actionType;
    private String channelId;
    private short rotateAngle;

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public short getRotateAngle() {
        return rotateAngle;
    }

    public void setRotateAngle(short rotateAngle) {
        this.rotateAngle = rotateAngle;
    }

}