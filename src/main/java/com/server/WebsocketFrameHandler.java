package com.server;

import com.event.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmax.disruptor.RingBuffer;
import com.util.GameEventMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

public class WebsocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final RingBuffer<GameEvent> ringBuffer;
    private final ObjectMapper objectMapper;

    public WebsocketFrameHandler(RingBuffer<GameEvent> ringBuffer, ObjectMapper objectMapper) {
        this.ringBuffer = ringBuffer;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String message = frame.text();
        if(!message.contains("PING")) {
            List<SmallGameEvent> smallGameEvent = objectMapper.readValue(message, new TypeReference<List<SmallGameEvent>>() {
            });
            smallGameEvent.forEach(gameEvent -> gameEvent.setChannel(ctx.channel()));
            publishToDisruptor(smallGameEvent);
        }else {
            ctx.fireChannelRead(frame.retain());
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);

        SmallGameEvent smallGameEvent = new SmallGameEvent();
        smallGameEvent.setActionType(ActionType.CONNECT);
        smallGameEvent.setChannel(ctx.channel());
        publishToDisruptor(smallGameEvent);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);

        SmallGameEvent smallGameEvent = new SmallGameEvent();
        smallGameEvent.setActionType(ActionType.DISCONNECT);
        smallGameEvent.setChannel(ctx.channel());
        publishToDisruptor(smallGameEvent);
    }

    private void publishToDisruptor(SmallGameEvent smallGameEvent) {
        long sequence = ringBuffer.next();
        try {
            GameEvent event = ringBuffer.get(sequence);
            GameEventMapper.toGameEvent(smallGameEvent, event);
        } finally {
            ringBuffer.publish(sequence);
        }
    }

    private void publishToDisruptor(List<SmallGameEvent> smallGameEvent) {
        for (SmallGameEvent gameEvent : smallGameEvent) {
            long sequence = ringBuffer.next();
            try {
                GameEvent event = ringBuffer.get(sequence);
                GameEventMapper.toGameEvent(gameEvent, event);
            } finally {
                ringBuffer.publish(sequence);
            }
        }

    }

}
