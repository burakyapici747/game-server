package com.server;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
import com.component.system.MovementSystem;
import com.component.system.NetworkingSystem;
import com.component.system.SnakeBodySystem;
import com.event.*;
import com.game.Game;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketServer {
    private static final int PORT = 8080;
    private static final int DISRUPTOR_BUFFER_SIZE = 1024;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private final Map<String, Integer> componentsByChannelId = new ConcurrentHashMap<>();
    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private Disruptor<GameEvent> disruptor;
    private World world;

    public WebSocketServer() {
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
    }

    public void start() {
        try {
            setupComponentWorld();
            setupDisruptor();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WebSocketServerInitializer(world, disruptor.getRingBuffer(), componentsByChannelId, channels))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            System.out.println("WebSocket server started on port " + PORT + ".");
            ChannelFuture channelFuture = bootstrap.bind(PORT).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            System.err.println("An error occurred while starting the server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Shutting down the server...");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            if (disruptor != null) {
                disruptor.shutdown();
            }
        }
    }

    private void setupComponentWorld() {
        System.out.println("Setting up Artemis ECS world...");
        WorldConfiguration configuration = new WorldConfigurationBuilder()
            .with(new SnakeBodySystem())
            .with(new MovementSystem())
            .with(new NetworkingSystem())
            .build();
        world = new World(configuration);
        System.out.println("Artemis ECS world set up.");
    }

    private void setupDisruptor() {
        System.out.println("Setting up LMAX Disruptor...");
        Game game = new Game(channels, world, this.componentsByChannelId);
        Thread gameThread = new Thread(game);
        gameThread.setName("Game-Loop-Thread");
        gameThread.start();

        EventFactory<GameEvent> factory = () -> new GameEvent(game);

        disruptor = new Disruptor<>(
            factory,
            DISRUPTOR_BUFFER_SIZE,
            DaemonThreadFactory.INSTANCE,
            ProducerType.MULTI,
            new SleepingWaitStrategy()
        );

        PlayerConnectEvent playerConnectEvent = new PlayerConnectEvent();
        PlayerDisconnectEvent playerDisconnectEvent = new PlayerDisconnectEvent();
        PlayerInputEvent playerInputEvent = new PlayerInputEvent();
        PhysicEvent physicEvent = new PhysicEvent();

        disruptor.handleEventsWith(playerConnectEvent, playerDisconnectEvent, playerInputEvent);
        disruptor.after(playerInputEvent).then(physicEvent);

        disruptor.start();
        System.out.println("LMAX Disruptor started.");
    }
}