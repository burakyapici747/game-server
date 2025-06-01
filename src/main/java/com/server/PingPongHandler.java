package com.server;

import com.event.ActionType;
import com.event.data.ClientData;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class PingPongHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final ObjectMapper objectMapper;

    public PingPongHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        try {
            String message = frame.text();
            ClientData incomingData = objectMapper.readValue(message, ClientData.class);
            if (incomingData != null && ActionType.PING.equals(incomingData.getActionType())) {
                ClientData clientData = new ClientData();
                clientData.setServerTimestamp(System.currentTimeMillis());
                clientData.setActionType(ActionType.PONG);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(objectMapper.writeValueAsString(clientData)));
            }
        }catch (Exception e){

        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.out.println(cause.getMessage());
    }
}
