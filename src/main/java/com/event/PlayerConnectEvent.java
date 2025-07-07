package com.event;

import com.artemis.World;
import com.component.*;
import com.component.PhysicBodyComponent;
import com.game.PlayerBuilder;
import com.game.StartInformationDto;
import com.lmax.disruptor.EventHandler;
import io.netty.channel.Channel;

public class PlayerConnectEvent implements EventHandler<GameEvent> {
    @Override
    public void onEvent(GameEvent gameEvent, long l, boolean b) {
        if (gameEvent != null && ActionType.CONNECT.equals(gameEvent.getActionType())) {
            int componentId = gameEvent.getWorld().create();
            StartInformationDto startInformation = editComponent(gameEvent.getWorld(), componentId, gameEvent.getChannel());
            gameEvent.getComponentsByChannelId().put(gameEvent.getChannel().id().asLongText(), componentId);
            gameEvent.getGame().addPlayer(gameEvent, startInformation);
        }
    }

    private StartInformationDto editComponent(World world, int entityId, Channel channel) {
//        Body body = new Body();
//        body.addFixture(
//                Geometry.createCircle(0.3),
//                0.0,
//                0.0,
//                0.0
//        );
//        body.setMass(MassType.NORMAL);
//        body.setEnabled(true);
//        body.getTransform().setTranslation(new Vector2(5, 5));
//        body.setAtRestDetectionEnabled(false);

        PlayerBuilder playerBuilder = new PlayerBuilder();
        StartInformationDto startInformationDTO = playerBuilder.createPlayer(100, 0.5, entityId);

        world.edit(entityId)
                .add(new AngleComponent())
                .add(new NettyChannelComponent(channel, 0))
                .add(new PlayerTagComponent())
                .add(new PositionComponent(startInformationDTO.startPosition().x, startInformationDTO.startPosition().y))
                .add(new VelocityComponent())
                .add(new PhysicBodyComponent(startInformationDTO.playerSnake()));
        return startInformationDTO;
    }
}