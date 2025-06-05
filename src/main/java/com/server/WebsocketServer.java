package com.server;

import com.Player;
import com.event.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.Game;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketServer {
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    // LMAX Disruptor
    private Disruptor<GameEvent> disruptor;
    private RingBuffer<GameEvent> ringBuffer;

    // Websocket handler referansı (mesaj gönderimi için)
    private WebsocketFrameHandler websocketHandler;

    public static final Map<String, Player> activePlayerMap = new ConcurrentHashMap<>();
    public static final Map<String, Channel> playerChannelMap = new ConcurrentHashMap<>();

    public WebsocketServer() {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
    }

    public void startServer() throws Exception {
        try {
            setupDisruptor();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            ObjectMapper objectMapper = new ObjectMapper();

                            WebsocketFrameHandler websocketFrameHandler = new WebsocketFrameHandler(ringBuffer, objectMapper);
                            PingPongHandler pingPongHandler = new PingPongHandler(objectMapper);

                            //websocket destekli pipeline yapilandirmasi
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));//Websocket handshake islemleri icin gerekli
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));//websocket upgrade ve frame yonetimi
                            pipeline.addLast(pingPongHandler);

                        }
                    })
                    .childOption(ChannelOption.TCP_NODELAY, true);
            System.out.println("channel init");
            ChannelFuture channelFuture = bootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (RuntimeException e) {
            System.out.println("Error starting server: " + e.getMessage());
        } finally {
            // Shutdown the event loop groups
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void setupDisruptor() {
        //GameEvent icerisinde kullanmak uzere bir Game referansi olusturuldu
        Game game = new Game();
        //RingBuffer'da tutulacak GameEvent'in constructur'ina yukarida olusturulan Game nesnesini pass'layan factory
        EventFactory<GameEvent> factory = () -> new GameEvent(game);
        disruptor = new Disruptor<>(
                factory,
                262144,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new YieldingWaitStrategy()
        );

        //disruptor uzerinden generate edilmis ringBuffer'in setle
        ringBuffer = disruptor.getRingBuffer();

        //Eventleri olustur ve siralamalari ayarla
        PlayerConnectEvent playerConnectEvent = new PlayerConnectEvent();
        PlayerDisconnectEvent playerDisconnectEvent = new PlayerDisconnectEvent();
        PlayerInputEvent playerInputEvent = new PlayerInputEvent();
        PhysicEvent physicEvent = new PhysicEvent();

        disruptor.handleEventsWith(playerConnectEvent, playerDisconnectEvent, playerInputEvent, physicEvent);
        disruptor.after(playerInputEvent)
                .then(physicEvent);

        //Disruptor'u baslat
        disruptor.start();
    }
}
