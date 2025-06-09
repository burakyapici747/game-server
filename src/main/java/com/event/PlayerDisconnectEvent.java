package com.event;

import com.lmax.disruptor.EventHandler;
import com.server.WebsocketServer;

public class PlayerDisconnectEvent implements EventHandler<GameEvent> {
    @Override
    public void onEvent(GameEvent gameEvent, long l, boolean b) {
        if (gameEvent != null && ActionType.DISCONNECT.equals(gameEvent.getActionType())) {
            gameEvent.getGame().removePlayer(gameEvent);
            WebsocketServer.activePlayerMap.remove(gameEvent.getChannel().id().toString());
        }
    }
}
