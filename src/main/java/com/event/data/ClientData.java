package com.event.data;

import com.event.ActionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientData {
    private ActionType actionType;
    private Long serverTimestamp;
    private Long clientTimestamp;

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

    public Long getClientTimestamp() {
        return clientTimestamp;
    }

    public void setClientTimestamp(Long clientTimestamp) {
        this.clientTimestamp = clientTimestamp;
    }
}
