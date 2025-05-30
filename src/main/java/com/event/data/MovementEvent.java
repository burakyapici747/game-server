package com.event.data;

import com.event.GameEvent;

public class MovementEvent extends GameEvent {
    private Float x;
    private Float y;
    private Short rotateAngle;

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Short getRotateAngle() {
        return rotateAngle;
    }

    public void setRotateAngle(Short rotateAngle) {
        this.rotateAngle = rotateAngle;
    }
}
