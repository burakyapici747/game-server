package com.event.data;

import com.event.GameEvent;

public class FireEvent extends GameEvent {
    private Boolean isFired;

    public Boolean getFired() {
        return isFired;
    }

    public void setFired(Boolean fired) {
        isFired = fired;
    }
}
