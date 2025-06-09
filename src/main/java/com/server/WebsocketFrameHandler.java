package com.server;

import client.ClientDataOuterClass;
import com.event.*;
import com.lmax.disruptor.RingBuffer;
import com.util.GameEventMapper;
import envelope.EnvelopeOuterClass;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class WebsocketFrameHandler extends SimpleChannelInboundHandler<envelope.EnvelopeOuterClass.Envelope> {
    private final RingBuffer<GameEvent> ringBuffer;

    public WebsocketFrameHandler(RingBuffer<GameEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EnvelopeOuterClass.Envelope envelope) {
        switch (envelope.getPayloadCase()) {
            case CLIENTDATA -> {
                publishToDisruptor(envelope.getClientData(), ctx.channel());
            }
            default -> {
                //TODO: Move ve ping disinda client'dan gelecek farkli tiplerdeki mesajlar buraya duser...
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
            event.setActionType(ActionType.CONNECT);
            event.setChannel(channel);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    private void publishToDisruptorForDisconnectedUser(Channel channel) {
        long sequence = ringBuffer.next();
        try {
            GameEvent event = ringBuffer.get(sequence);
            event.setActionType(ActionType.DISCONNECT);
            event.setChannel(channel);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    private void publishToDisruptor(ClientDataOuterClass.ClientData clientData, Channel channel) {
        long sequence = ringBuffer.next();
        try {
            GameEvent event = ringBuffer.get(sequence);
            GameEventMapper.toGameEvent(clientData, event, channel);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

}
