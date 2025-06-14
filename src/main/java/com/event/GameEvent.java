package com.event;

import com.game.Game;
import io.netty.channel.Channel;

public class GameEvent {
    private final Game game;
    private ActionType actionType;
    private Channel channel;
    private Double dx;
    private Double dy;
    private Integer sequenceId;
    private Long clientTimestampOffset;
    private float deltaTime;

    public GameEvent(Game game) {
        this.game = game;
    }

    public Double getDx() {
        return dx;
    }

    public void setDx(Double dx) {
        this.dx = dx;
    }

    public Double getDy() {
        return dy;
    }

    public void setDy(Double dy) {
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

    public Integer getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Integer sequenceId) {
        this.sequenceId = sequenceId;
    }

    public Game getGame() {
        return game;
    }

    public Long getClientTimestampOffset() {
        return clientTimestampOffset;
    }

    public void setClientTimestampOffset(Long clientTimestampOffset) {
        this.clientTimestampOffset = clientTimestampOffset;
    }

    public float getDeltaTime() {
        return this.deltaTime;
    }

    public void setDeltaTime(float deltaTime) {
        this.deltaTime = deltaTime;
    }
}

