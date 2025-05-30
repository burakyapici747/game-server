package com.game;

import com.Player;
import com.event.ActionType;
import com.event.GameEvent;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Game {
    private final World<Body> world;
    public final Map<String, Player> playersByChannelId = new ConcurrentHashMap<>();

    public Game() {
        //TODO: Initialize body count limit eklenebilir.
        this.world = new World<>();
        this.world.setGravity(World.ZERO_GRAVITY);
    }

    public Body addPlayer(GameEvent gameEvent) {
        Body playerBody = new Body();

        if (ActionType.CONNECT.equals(gameEvent.getActionType())) {

            playerBody.addFixture(
                    Geometry.createCircle(0.5),
                    1.0,
                    0.4, 0.4
            );
            playerBody.setMass(MassType.NORMAL);
            this.world.addBody(playerBody);

            return playerBody;
        }

        return null;
    }

    public void removePlayer(GameEvent gameEvent) {
        if (ActionType.DISCONNECT.equals(gameEvent.getActionType())) {
            //TODO: World'den disconnect olan player'in body'sini kaldir!!!A
            this.world.removeBody(this.playersByChannelId.get(gameEvent.getChannel().id().asLongText()).getBody());
        }
    }

    public void applyMove(GameEvent gameEvent) {
        if(ActionType.MOVE.equals(gameEvent.getActionType()) /*&& gameEvent.get*/) {

        }
    }
}
