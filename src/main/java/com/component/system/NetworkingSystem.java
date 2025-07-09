package com.component.system;

import client.ClientDataOuterClass;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.artemis.utils.IntBag;
import com.component.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import server.GameStateOuterClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NetworkingSystem extends BaseSystem {
    protected ComponentMapper<AngleComponent> angleMapper;
    protected ComponentMapper<PositionComponent> positionMapper;
    protected ComponentMapper<VelocityComponent> velocityMapper;
    protected ComponentMapper<NettyChannelComponent> channelMapper;

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(4096);

    @All(PlayerTagComponent.class)
    private EntitySubscription players;

    public NetworkingSystem() {}

    @Override
    protected void initialize() {
        super.initialize();
        this.positionMapper = world.getMapper(PositionComponent.class);
        this.velocityMapper = world.getMapper(VelocityComponent.class);
        this.channelMapper = world.getMapper(NettyChannelComponent.class);
        this.angleMapper = world.getMapper(AngleComponent.class);
    }

    @Override
    protected void processSystem() {
        IntBag playerIds = players.getEntities();
        if (playerIds.isEmpty()) {
            return;
        }

        List<GameStateOuterClass.EntityState> entityStates = buildWorldState(playerIds);


        for (int i = 0, s = playerIds.size(); i < s; i++) {
            int targetPlayerId = playerIds.get(i);
            NettyChannelComponent channelComponent = channelMapper.get(targetPlayerId);

            if (channelComponent == null || channelComponent.channel == null || !channelComponent.channel.isActive()) {
                continue;
            }

            GameStateOuterClass.GameState gameState = GameStateOuterClass.GameState.newBuilder()
                .addAllEntities(entityStates)
                .setClientId(targetPlayerId)
                .build();

            server.ServerEnvelopeOuterClass.ServerEnvelope serverEnvelope = server.ServerEnvelopeOuterClass.ServerEnvelope.newBuilder()
                .setActionType(ClientDataOuterClass.ActionType.GAME_STATE)
                .setGameState(gameState)
                .build();

            try {
                byteArrayOutputStream.reset();
                serverEnvelope.writeDelimitedTo(byteArrayOutputStream);
                ByteBuf payload = Unpooled.wrappedBuffer(byteArrayOutputStream.toByteArray());
                channelComponent.channel.writeAndFlush(new BinaryWebSocketFrame(payload));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private List<GameStateOuterClass.EntityState> buildWorldState(IntBag playerIds) {
        int size = playerIds.size();
        List<GameStateOuterClass.EntityState> states = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            int entityId = playerIds.get(i);

            PositionComponent position = positionMapper.get(entityId);
            VelocityComponent velocity = velocityMapper.get(entityId);
            AngleComponent angle = angleMapper.get(entityId);

            GameStateOuterClass.EntityState entityState = GameStateOuterClass.EntityState.newBuilder()
                .setId(entityId)
                .setX(position.x)
                .setY(position.y)
                .setVx(velocity.dx)
                .setVy(velocity.dy)
                .setAngle((int)angle.angle)
                .build();
            states.add(entityState);
        }
        return states;
    }
}
