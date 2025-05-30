package com.server;

import com.event.*;
import com.lmax.disruptor.RingBuffer;
import com.util.GameEventMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebsocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final RingBuffer<GameEvent> ringBuffer;

    public WebsocketFrameHandler(RingBuffer<GameEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String message = frame.text();
        String channelId = ctx.channel().id().asLongText();

        SmallGameEvent smallGameEvent = new SmallGameEvent();

        publishToDisruptor(smallGameEvent);
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

}
