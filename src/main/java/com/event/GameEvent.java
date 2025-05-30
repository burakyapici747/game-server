package com.event;

import com.game.Game;
import io.netty.channel.Channel;

public class GameEvent {
    private final Game game;
    private ActionType actionType;
    private Channel channel;
    private Short rotateAngle;
    private Float x;
    private Float y;
    private Long sequenceId;

    public GameEvent(Game game) {
        this.game = game;
    }

    public Short getRotateAngle() {
        return rotateAngle;
    }

    public void setRotateAngle(Short rotateAngle) {
        this.rotateAngle = rotateAngle;
    }

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

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public Game getGame() {
        return game;
    }
}

