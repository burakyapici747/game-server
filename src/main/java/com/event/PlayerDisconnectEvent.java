package com.event;

import com.lmax.disruptor.EventHandler;

public class PlayerDisconnectEvent implements EventHandler<GameEvent> {
    @Override
    public void onEvent(GameEvent gameEvent, long l, boolean b) {
        if (gameEvent != null && ActionType.DISCONNECT.equals(gameEvent.getActionType())) {
            gameEvent.getGame().removePlayer(gameEvent);
            gameEvent.getWorld().delete(gameEvent.getComponentsByChannelId().get(gameEvent.getChannel().id().asLongText()));
            gameEvent.getComponentsByChannelId().remove(gameEvent.getChannel().id().asLongText());
        }
    }
}
