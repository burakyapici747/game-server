package com.server;

import client.ClientDataOuterClass;
import envelope.EnvelopeOuterClass;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import server.Pong;

import java.io.ByteArrayOutputStream;

public class PingPongHandler extends SimpleChannelInboundHandler<EnvelopeOuterClass.Envelope> {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EnvelopeOuterClass.Envelope env) throws Exception {
        switch (env.getPayloadCase()) {
            case PINGDATA -> {
                var pingData = env.getPingData();

                Pong.PongData pongData = Pong.PongData.newBuilder()
                        .setActionType(ClientDataOuterClass.ActionType.PONG)
                        .setServerTimestamp(System.currentTimeMillis())
                        .setClientTimestamp(pingData.getTimestamp())
                        .setNonce(pingData.getNonce())
                        .build();

                server.ServerEnvelopeOuterClass.ServerEnvelope serverEnv =
                        server.ServerEnvelopeOuterClass.ServerEnvelope.newBuilder()
                                .setActionType(ClientDataOuterClass.ActionType.PONG)
                                .setPongData(pongData)
                                .build();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                serverEnv.writeDelimitedTo(baos);

                ByteBuf buf = Unpooled.wrappedBuffer(baos.toByteArray());
                ctx.writeAndFlush(new BinaryWebSocketFrame(buf));
            }
            default -> {
                ctx.fireChannelRead(env);
            }
        }
    }
}