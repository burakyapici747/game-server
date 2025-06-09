package com.event;

import com.Player;
import com.lmax.disruptor.EventHandler;
import com.server.WebsocketServer;

public class PlayerConnectEvent implements EventHandler<GameEvent> {
    @Override
    public void onEvent(GameEvent gameEvent, long l, boolean b) {
        if (gameEvent != null && ActionType.CONNECT.equals(gameEvent.getActionType())) {
            Player player = new Player();
            player.setChannelId(gameEvent.getChannel().id().asLongText());
            player.setName(gameEvent.getChannel().id().asLongText());
            player.setBody(gameEvent.getGame().addPlayer(gameEvent));
            gameEvent.getGame().playersByChannelId.put(gameEvent.getChannel().id().asLongText(), player);
            WebsocketServer.activePlayerMap.put(gameEvent.getChannel().id().toString(), player);
        }
    }
}
