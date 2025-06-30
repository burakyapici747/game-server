package com.event;

import com.artemis.World;
import com.component.*;
import com.component.PhysicBodyComponent;
import com.lmax.disruptor.EventHandler;
import io.netty.channel.Channel;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

public class PlayerConnectEvent implements EventHandler<GameEvent> {
    @Override
    public void onEvent(GameEvent gameEvent, long l, boolean b) {
        if (gameEvent != null && ActionType.CONNECT.equals(gameEvent.getActionType())) {
            int componentId = gameEvent.getWorld().create();
            editComponent(gameEvent.getWorld(), componentId, gameEvent.getChannel());
            gameEvent.getComponentsByChannelId().put(gameEvent.getChannel().id().asLongText(), componentId);
            gameEvent.getGame().addPlayer(gameEvent);
        }
    }

    private void editComponent(World world, int componentId, Channel channel) {
        Body body = new Body();
        body.addFixture(
                Geometry.createCircle(0.3),
                0.0,
                0.0,
                0.0
        );
        body.setMass(MassType.NORMAL);
        body.setEnabled(true);
        body.getTransform().setTranslation(new Vector2(5, 5));
        body.setAtRestDetectionEnabled(false);

        world.edit(componentId)
                .add(new AngleComponent())
                .add(new NettyChannelComponent(channel, 0))
                .add(new PlayerTagComponent())
                .add(new PositionComponent(5.0, 5.0))
                .add(new VelocityComponent())
                .add(new PhysicBodyComponent(body));
    }
}