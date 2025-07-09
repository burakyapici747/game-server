package com.server;

import client.ClientDataOuterClass;
import envelope.EnvelopeOuterClass;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import server.Pong;
import server.ServerEnvelopeOuterClass;

import java.io.ByteArrayOutputStream;

@ChannelHandler.Sharable
public class PingPongHandler extends SimpleChannelInboundHandler<EnvelopeOuterClass.Envelope> {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EnvelopeOuterClass.Envelope env) throws Exception {
        if (env.getPayloadCase() == EnvelopeOuterClass.Envelope.PayloadCase.PINGDATA) {
            var pingData = env.getPingData();
            Pong.PongData pongData = Pong.PongData.newBuilder()
                    .setServerTimestamp(System.currentTimeMillis())
                    .setClientTimestamp(pingData.getTimestamp())
                    .setNonce(pingData.getNonce())
                    .build();

            ServerEnvelopeOuterClass.ServerEnvelope serverEnv =
                    ServerEnvelopeOuterClass.ServerEnvelope.newBuilder()
                            .setActionType(ClientDataOuterClass.ActionType.PONG)
                            .setPongData(pongData)
                            .build();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            serverEnv.writeDelimitedTo(baos);

            ByteBuf buf = Unpooled.wrappedBuffer(baos.toByteArray());
            ctx.writeAndFlush(new BinaryWebSocketFrame(buf));
        } else {
            ctx.fireChannelRead(env);
        }
    }
}