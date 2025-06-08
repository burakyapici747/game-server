package com.game;

import com.DrawingPanel;
import com.Player;
import com.event.ActionType;
import com.event.GameEvent;
import com.event.data.Input;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;
import server.GameStateOuterClass;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;

public class Game implements Runnable{
    private final double DT = 1 / 60.0;
    private final World<Body> world;
    private final ChannelGroup channels;
    public final Map<String, Player> playersByChannelId = new ConcurrentHashMap<>();


    // Yeni: Görselleştirme için Swing bileşenleri
    private JFrame frame;
    private DrawingPanel drawingPanel;

    //TODO: Daha performansli bir veri yapisinda saklanacaklar!!!
    public final PriorityBlockingQueue<Input> inputBuffer = new PriorityBlockingQueue<>(
        11, Comparator.comparingLong(Input::getClientTimestampOffset)
    );
    //En son hangi timestamp degeri islendi, lastServerTickEndTimestamp + TICK_RATE_IN_MS degeri koyulacak
    public Long lastServerTickEndTimestamp = 0L;

    public Game(ChannelGroup channels) {
        this.channels = channels;
        //TODO: Initialize body count limit eklenebilir.
        this.world = new World<>();
        this.world.setGravity(World.ZERO_GRAVITY);
        // 2) Görselleştirme penceresini başlat
        SwingUtilities.invokeLater(this::initializeVisualization);
    }

    private void initializeVisualization() {
        // JFrame oluştur
        this.frame = new JFrame("Game Physics Viewer");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Dyn4j World’u gören bir DrawingPanel ekle
        this.drawingPanel = new DrawingPanel(this.world);
        this.frame.add(this.drawingPanel);
        this.frame.pack();
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);

        // Her tick sonrasında panel’i tekrar çiz (repaint) et
        // Bunu da ayrı bir scheduler ya da aynı tickScheduler içine ekleyebilirsiniz:
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(false);
            return t;
        }).scheduleAtFixedRate(() -> {
            if (this.drawingPanel != null) {
                this.drawingPanel.repaint();
            }
        }, 0L, 16, TimeUnit.MILLISECONDS);
    }

    public void addInput(Input input) {
        if (input.getClientTimestampOffset() != null) {
            this.inputBuffer.offer(input);
        }
    }

    public Body addPlayer(GameEvent gameEvent) {
        Body playerBody = new Body();

        if (ActionType.CONNECT.equals(gameEvent.getActionType())) {
            Player newPlayer = new Player();
            newPlayer.setBody(playerBody);
            newPlayer.setName("test");
            newPlayer.setChannelId(gameEvent.getChannel().id().asLongText());
            this.playersByChannelId.put(gameEvent.getChannel().id().asLongText(), newPlayer);
            Random rnd = new Random();
            double randDouble = 0.1 + rnd.nextDouble() * (1.0 - 0.1);
            playerBody.addFixture(
                Geometry.createCircle(randDouble),
                1.0,
                0.4,
                0.4
            );
            playerBody.setMass(MassType.NORMAL);
            playerBody.setEnabled(true);
            playerBody.getTransform().setTranslation(new Vector2(10, 10));
            playerBody.setAtRestDetectionEnabled(false);
            this.world.addBody(playerBody);

            return playerBody;
        }

        return null;
    }

    public void removePlayer(GameEvent gameEvent) {
        if (ActionType.DISCONNECT.equals(gameEvent.getActionType())) {
            if (gameEvent.getChannel() != null) {
                this.world.removeBody(this.playersByChannelId.get(gameEvent.getChannel().id().asLongText()).getBody());
                this.playersByChannelId.remove(gameEvent.getChannel().id().asLongText());
            }
        }
    }

    private void broadcastGameState(long timestampMs) {
        //TODO: parametre ile gelen timestampMs ileride kullanilmasi gerekebilir!!!
        GameStateOuterClass.GameState.Builder gs = GameStateOuterClass.GameState.newBuilder();

        for (Player p : playersByChannelId.values()) {
            Body b = p.getBody();
            Vector2 pos = b.getTransform().getTranslation();
            Vector2 vel = b.getLinearVelocity();
            gs.addEntities(
                GameStateOuterClass.EntityState.newBuilder()
                    .setId(p.getChannelId())
                    .setX(pos.x).setY(pos.y)
                    .setVx(vel.x).setVy(vel.y)
                    .build()
            );
        }

        System.out.println(channels.size());

        ByteBuf buf = Unpooled.wrappedBuffer(gs.build().toByteArray());
        channels.writeAndFlush(new BinaryWebSocketFrame(buf));
    }

    @Override
    public void run() {
        double accumulator = 0.0;
        double t = 0.0;
        long lastTime = System.nanoTime();
        long simulationStartTimeMs = System.currentTimeMillis();

        while(true){

            long now = System.nanoTime();
            double frameTime = (now - lastTime) / 1e9;
            lastTime = now;

            if(frameTime > 0.25) frameTime  = 0.25;

            accumulator += frameTime;

            while (accumulator >= DT) {
                accumulator -= DT;
                t  += DT;

                long simTimeMs = simulationStartTimeMs + (long)(t * 1000);

                Input in;
                while ((in = inputBuffer.peek()) != null
                       && in.getClientTimestampOffset() <= simTimeMs) {
                    inputBuffer.poll();
                    Player player = playersByChannelId.get(in.getChannelId());

                    if (player != null) {
                        Body body = player.getBody();

                        body.setLinearVelocity(in.getDx() * 5, in.getDy() * 5);
                    }
                }

                world.step(1, DT);
                broadcastGameState(System.currentTimeMillis());
            }

            try {
                //TODO: Thread, sleep 1 gercekten gerekli mi?
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

    }
}
