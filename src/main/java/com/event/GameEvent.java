package com.event;

import com.game.Game;
import io.netty.channel.Channel;

public class GameEvent {
    private final Game game;
    private ActionType actionType;
    private Channel channel;
    private Long timestamp;
    private Short rotateAngle;
    private Float dx;
    private Float dy;
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

    public Float getDx() {
        return dx;
    }

    public void setDx(Float dx) {
        this.dx = dx;
    }

    public Float getDy() {
        return dy;
    }

    public void setDy(Float dy) {
        this.dy = dy;
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

