package com.server;

import client.ClientDataOuterClass;
import com.artemis.World;
import com.component.*;
import com.event.*;
import com.lmax.disruptor.RingBuffer;
import com.util.GameEventMapper;
import envelope.EnvelopeOuterClass;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<envelope.EnvelopeOuterClass.Envelope> {
    private final RingBuffer<GameEvent> ringBuffer;
    private final World world;
    private final Map<String, Integer> componentsByChannelId;

    public WebSocketFrameHandler(RingBuffer<GameEvent> ringBuffer, World world, Map<String, Integer> componentsByChannelId) {
        this.ringBuffer = ringBuffer;
        this.world = world;
        this.componentsByChannelId = componentsByChannelId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EnvelopeOuterClass.Envelope envelope) {
        switch (envelope.getPayloadCase()) {
            case CLIENTDATA -> {
                publishToDisruptor(envelope.getClientData(), ctx.channel());
            }
            default -> {
                System.out.println("PING VE MOVE DISINDA CLIENT'DAN BIR PAKET GELDI!!!");
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        publishToDisruptorForConnectedUser(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        publishToDisruptorForDisconnectedUser(ctx.channel());
    }

    private void publishToDisruptorForConnectedUser(Channel channel) {
        long sequence = ringBuffer.next();
        try {
            GameEvent event = ringBuffer.get(sequence);
            event.setWorld(world);
            event.setActionType(ActionType.CONNECT);
            event.setChannel(channel);
            event.setComponentsByChannelId(this.componentsByChannelId);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    private void publishToDisruptorForDisconnectedUser(Channel channel) {
        long sequence = ringBuffer.next();
        try {
            GameEvent event = ringBuffer.get(sequence);
            event.setWorld(world);
            event.setActionType(ActionType.DISCONNECT);
            event.setChannel(channel);
            event.setComponentsByChannelId(componentsByChannelId);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    private void publishToDisruptor(ClientDataOuterClass.ClientData clientData, Channel channel) {
        long sequence = ringBuffer.next();
        try {
            GameEvent event = ringBuffer.get(sequence);
            event.setWorld(world);
            GameEventMapper.toGameEvent(clientData, event, channel);
            event.setComponentsByChannelId(componentsByChannelId);
        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
