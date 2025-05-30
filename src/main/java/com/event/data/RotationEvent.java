package com.event.data;

import com.event.GameEvent;

public class RotationEvent extends GameEvent {
    private short angle;

    public short getAngle() {
        return angle;
    }

    public void setAngle(short angle) {
        this.angle = angle;
    }
}
