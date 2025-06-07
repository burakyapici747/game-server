package com.event;

import com.lmax.disruptor.EventHandler;
import com.server.WebsocketServer;

public class PlayerDisconnectEvent implements EventHandler<GameEvent> {
    @Override
    public void onEvent(GameEvent gameEvent, long l, boolean b) throws Exception {
        if(gameEvent != null && ActionType.DISCONNECT.equals(gameEvent.getActionType())) {
            WebsocketServer.activePlayerMap.remove(gameEvent.getChannel().id().toString());
            gameEvent.getGame().removePlayer(gameEvent);
        }
    }
}
