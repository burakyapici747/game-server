package com.component.system;

import client.ClientDataOuterClass;
import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.component.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import server.GameStateOuterClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NetworkingSystem extends IteratingSystem {

    @All(PlayerTagComponent.class)
    private EntitySubscription players;
    protected ComponentMapper<PositionComponent> positionMapper;
    protected ComponentMapper<VelocityComponent> velocityMapper;
    protected ComponentMapper<NettyChannelComponent> channelMapper;

    public NetworkingSystem() {
        super(Aspect.all(PositionComponent.class, VelocityComponent.class, NettyChannelComponent.class));
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.positionMapper = world.getMapper(PositionComponent.class);
        this.velocityMapper = world.getMapper(VelocityComponent.class);
        this.channelMapper = world.getMapper(NettyChannelComponent.class);
    }

    @Override
    protected void process(int i) {
        server.GameStateOuterClass.GameState.Builder gameStateBuilder = server.GameStateOuterClass.GameState.newBuilder();

        for (int entityId : this.players.getEntities().getData()) {
            PositionComponent positionComponent = positionMapper.get(entityId);
            VelocityComponent velocityComponent = velocityMapper.get(entityId);
            gameStateBuilder
                    .addEntities(
                            GameStateOuterClass.EntityState.newBuilder()
                                    .setId(entityId)
                                    .setX(positionComponent.x)
                                    .setY(positionComponent.y)
                                    .setVx(velocityComponent.dx)
                                    .setVy(velocityComponent.dy)
                                    .build()
                    );
        }

        for (int entityId : this.players.getEntities().getData()) {
            NettyChannelComponent nettyChannelComponent = channelMapper.get(entityId);
            gameStateBuilder
                    .setLastProcessedSequenceId(nettyChannelComponent.lastProcessedSequenceId)
                    .setClientId(entityId)
                    .build();

            server.ServerEnvelopeOuterClass.ServerEnvelope.Builder serverEnvelopeBuilder =
                    server.ServerEnvelopeOuterClass.ServerEnvelope.newBuilder();

            server.ServerEnvelopeOuterClass.ServerEnvelope serverEnvelope = serverEnvelopeBuilder
                    .setActionType(ClientDataOuterClass.ActionType.GAME_STATE)
                    .setGameState(gameStateBuilder.build())
                    .build();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                serverEnvelope.writeDelimitedTo(baos);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ByteBuf payload = Unpooled.wrappedBuffer(baos.toByteArray());
            nettyChannelComponent.channel.writeAndFlush(new BinaryWebSocketFrame(payload));
        }
    }
}
