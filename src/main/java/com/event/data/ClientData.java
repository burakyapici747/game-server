package com.event.data;

import com.event.ActionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientData {
    private ActionType actionType;
    private Long serverTimestamp;

    public Long getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(Long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
}
