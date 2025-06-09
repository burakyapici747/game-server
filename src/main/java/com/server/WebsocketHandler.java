package com.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class WebsocketHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {
    private final ChannelGroup channels;

    public WebsocketHandler(ChannelGroup channels) {
        this.channels = channels;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) throws Exception {
        // retain() so downstream handlers can release it when they’re done
        ctx.fireChannelRead(frame.content().retain());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.channels.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.channels.remove(ctx.channel());
    }
}
