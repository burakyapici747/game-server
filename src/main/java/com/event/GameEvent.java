package com.event;

import com.artemis.World;
import com.game.Game;
import io.netty.channel.Channel;

import java.util.Map;

public class GameEvent {
    private final Game game;
    private World world;
    private ActionType actionType;
    private Channel channel;
    private Integer sequenceId;
    private Long clientTimestampOffset;
    private short rotateAngle;
    private Map<String, Integer> componentsByChannelId;

    public GameEvent(Game game) {
        this.game = game;
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

    public short getRotateAngle() {
        return rotateAngle;
    }

    public void setRotateAngle(short rotateAngle) {
        this.rotateAngle = rotateAngle;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public Map<String, Integer> getComponentsByChannelId() {
        return componentsByChannelId;
    }

    public void setComponentsByChannelId(Map<String, Integer> componentsByChannelId) {
        this.componentsByChannelId = componentsByChannelId;
    }
}

