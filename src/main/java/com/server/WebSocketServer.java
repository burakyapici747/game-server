package com.server;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.component.system.MovementSystem;
import com.component.system.NetworkingSystem;
import com.component.system.SnakeBodySystem;
import com.event.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.Game;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import envelope.EnvelopeOuterClass;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;
import java.util.Map;

public class WebSocketServer {
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private final Map<String, Integer> componentsByChannelId = new HashMap<>();

    // LMAX Disruptor
    private Disruptor<GameEvent> disruptor;
    private RingBuffer<GameEvent> ringBuffer;
    private ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private World world;

    public WebSocketServer() {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    public void startServer() throws Exception {
        try {
            setupComponentWorld();
            setupDisruptor();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            ObjectMapper objectMapper = new ObjectMapper();

                            WebSocketFrameHandler websocketFrameHandler = new WebSocketFrameHandler(ringBuffer, world, componentsByChannelId);
                            PingPongHandler pingPongHandler = new PingPongHandler();

                            //websocket destekli pipeline yapilandirmasi
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));//Websocket handshake islemleri icin gerekli
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));//websocket upgrade ve frame yonetimi
                            pipeline.addLast(new WebSocketHandler(channels));
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            pipeline.addLast(new ProtobufDecoder(EnvelopeOuterClass.Envelope.getDefaultInstance()));
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());
                            pipeline.addLast(pingPongHandler);
                            pipeline.addLast(websocketFrameHandler);

                        }
                    })
                    .childOption(ChannelOption.TCP_NODELAY, true);
            ChannelFuture channelFuture = bootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (RuntimeException e) {
            System.out.println("WebsocketServer icerisinde " + e.getMessage());
        } finally {
            // Shutdown the event loop groups
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void setupComponentWorld() {
        WorldConfiguration configuration = new WorldConfigurationBuilder()
                .with(new SnakeBodySystem())
                .with(new MovementSystem())
                .with(new NetworkingSystem())
                .build();
        world = new World(configuration);
    }

    private void setupDisruptor() {
        Game game = new Game(channels, world, this.componentsByChannelId);
        Thread th = new Thread(game);
        th.start();
        //RingBuffer'da tutulacak GameEvent'in constructur'ina yukarida olusturulan Game nesnesini pass'layan factory
        EventFactory<GameEvent> factory = () -> new GameEvent(game);
        disruptor = new Disruptor<>(
                factory,
                1024,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new SleepingWaitStrategy()
        );

        //disruptor uzerinden generate edilmis ringBuffer'in setle
        ringBuffer = disruptor.getRingBuffer();

        //Eventleri olustur ve siralamalari ayarla
        PlayerConnectEvent playerConnectEvent = new PlayerConnectEvent();
        PlayerDisconnectEvent playerDisconnectEvent = new PlayerDisconnectEvent();
        PlayerInputEvent playerInputEvent = new PlayerInputEvent();
        PhysicEvent physicEvent = new PhysicEvent();

        disruptor.handleEventsWith(playerConnectEvent, playerDisconnectEvent, playerInputEvent);
        disruptor.after(playerInputEvent)
                .then(physicEvent);

        //Disruptor'u baslat
        disruptor.start();
    }
}