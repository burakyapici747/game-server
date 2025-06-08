package com.server;

import client.ClientDataOuterClass;
import client.Ping;
import envelope.EnvelopeOuterClass;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import server.Pong;

public class PingPongHandler extends SimpleChannelInboundHandler<EnvelopeOuterClass.Envelope> {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EnvelopeOuterClass.Envelope env) throws Exception {
        System.out.println(env.getPingData().getActionType());
        switch (env.getPayloadCase()){
            case PINGDATA -> {
                System.out.println("Case icerisin");
                Ping.PingData pingData = env.getPingData();
                Pong.PongData pongData = Pong.PongData.newBuilder()
                    .setActionType(ClientDataOuterClass.ActionType.PONG)
                    .setServerTimestamp(System.currentTimeMillis())
                    .setClientTimestamp(pingData.getTimestamp())
                    .setNonce(pingData.getNonce())
                    .build();

                byte[] data = pongData.toByteArray();

                ByteBuf buf = Unpooled.wrappedBuffer(data);
                ctx.writeAndFlush(new BinaryWebSocketFrame(buf));
            }
            default -> {
                ctx.fireChannelRead(env);
            }
        }
    }
}

//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
//        try {
//            String message = frame.text();
//            if (message.contains("PING")) {
//                ClientData incomingData = objectMapper.readValue(message, ClientData.class);
//                if (incomingData != null && ActionType.PING.equals(incomingData.getActionType())) {
//                    ClientData clientData = new ClientData();
//                    clientData.setClientTimestamp(incomingData.getClientTimestamp());
//                    clientData.setServerTimestamp(System.currentTimeMillis());
//                    clientData.setActionType(ActionType.PONG);
//                    clientData.setNonce(incomingData.getNonce());
//                    clientData.setClientTimestampOffset(incomingData.getClientTimestampOffset());
//                    ctx.channel().writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(clientData)));
//                    return;
//                }
//
//            } else {
//                ctx.fireChannelRead(frame.retain());
//                return;
//            }
//        } catch (Exception e) {
//            ctx.fireChannelRead(frame.retain());
//            return;
//        }
//
//        ctx.fireChannelRead(frame.retain());
//    }