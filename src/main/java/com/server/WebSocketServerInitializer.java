package com.server;

import com.artemis.World;
import com.event.GameEvent;
import com.lmax.disruptor.RingBuffer;
import envelope.EnvelopeOuterClass;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.Map;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    private final RingBuffer<GameEvent> ringBuffer;
    private final World world;
    private final Map<String, Integer> componentsByChannelId;

    private final ProtobufEncoder protobufEncoder = new ProtobufEncoder();
    private final ProtobufVarint32LengthFieldPrepender lengthFieldPrepender = new ProtobufVarint32LengthFieldPrepender();
    private final PingPongHandler pingPongHandler = new PingPongHandler();
    private final ChannelGroup channels;

    public WebSocketServerInitializer(
        World world,
        RingBuffer<GameEvent> ringBuffer,
        Map<String, Integer> componentsByChannelId,
        ChannelGroup channels
    ) {
        this.ringBuffer = ringBuffer;
        this.world = world;
        this.componentsByChannelId = componentsByChannelId;
        this.channels = channels;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        pipeline.addLast(new WebSocketHandler(channels));
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new ProtobufDecoder(EnvelopeOuterClass.Envelope.getDefaultInstance()));
        pipeline.addLast(lengthFieldPrepender);
        pipeline.addLast(protobufEncoder);
        pipeline.addLast(pingPongHandler);
        pipeline.addLast(new WebSocketFrameHandler(ringBuffer, world, componentsByChannelId));
    }
}