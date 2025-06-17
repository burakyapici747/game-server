package com.game;

import client.ClientDataOuterClass;
import com.DrawingPanel;
import com.Player;
import com.event.ActionType;
import com.event.GameEvent;
import com.event.data.Input;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;
import server.ClientStateOuterClass;
import server.GameStateOuterClass;
import server.StartLocationOuterClass;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Game implements Runnable {
    private final double DT = 1 / 60.0;
    private final World<Body> world;
    private final ChannelGroup channels;
    public final Map<String, Player> playersByChannelId = new ConcurrentHashMap<>();


    // Yeni: Görselleştirme için Swing bileşenleri
    private JFrame frame;
    private DrawingPanel drawingPanel;

    //TODO: Daha performansli bir veri yapisinda saklanacaklar!!!
    public final PriorityBlockingQueue<Input> inputBuffer = new PriorityBlockingQueue<>(
            11, Comparator.comparingLong(Input::getSequenceId)
    );
    //En son hangi timestamp degeri islendi, lastServerTickEndTimestamp + TICK_RATE_IN_MS degeri koyulacak
    public Long lastServerTickEndTimestamp = 0L;

    public Game(ChannelGroup channels) {
        this.channels = channels;
        //TODO: Initialize body count limit eklenebilir.
        this.world = new World<>();
        this.world.setGravity(World.ZERO_GRAVITY);
        // createBoundedMap(world, 1000.0, 1000.0, 1.0);
        // 2) Görselleştirme penceresini başlat
        SwingUtilities.invokeLater(this::initializeVisualization);
    }

    private void createBoundedMap(World<Body> world, double width, double height, double thickness) {
        Body floor = new Body();
        floor.addFixture(new Rectangle(width + 2 * thickness, thickness));
        floor.translate(0, height);
        floor.setMass(MassType.INFINITE);
        world.addBody(floor);

        Body ceiling = new Body();
        ceiling.addFixture(new Rectangle(width + 2 * thickness, thickness));
        ceiling.translate(0, 0);
        ceiling.setMass(MassType.INFINITE);
        world.addBody(ceiling);

        Body leftWall = new Body();
        leftWall.addFixture(new Rectangle(thickness, height));
        leftWall.translate(0, 0);
        leftWall.setMass(MassType.INFINITE);
        world.addBody(leftWall);

        Body rightWall = new Body();
        rightWall.addFixture(new Rectangle(thickness, height));
        rightWall.getTransform().setTranslation(width, 0);
        rightWall.setMass(MassType.INFINITE);
        world.addBody(rightWall);
    }

    private void initializeVisualization() {
        this.frame = new JFrame("Game Physics Viewer");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.drawingPanel = new DrawingPanel(this.world);
        this.frame.add(this.drawingPanel);
        this.frame.pack();
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);

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
        try {
            Body playerBody = new Body();

            if (ActionType.CONNECT.equals(gameEvent.getActionType())) {
                Player newPlayer = new Player();
                newPlayer.setBody(playerBody);
                newPlayer.setName("test");
                newPlayer.setChannelId(gameEvent.getChannel().id().asLongText());
                this.playersByChannelId.put(gameEvent.getChannel().id().asLongText(), newPlayer);
                playerBody.addFixture(
                        Geometry.createCircle(0.3),
                        1.0,
                        0.4,
                        0.4
                );
                playerBody.setMass(MassType.NORMAL);
                playerBody.setEnabled(true);
                sendStartLocationToPlayer(gameEvent.getChannel(), 5, 5);
                playerBody.getTransform().setTranslation(new Vector2(5, 5));
                playerBody.setAtRestDetectionEnabled(false);
                this.world.addBody(playerBody);
                return playerBody;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private void sendStartLocationToPlayer(Channel channel, double x, double y) throws IOException {
        if (channel != null) {
            StartLocationOuterClass.StartLocation.Builder startLocationBuilder = StartLocationOuterClass.StartLocation.newBuilder();
            startLocationBuilder.setX(x);
            startLocationBuilder.setY(y);

            server.ServerEnvelopeOuterClass.ServerEnvelope envelope =
                server.ServerEnvelopeOuterClass.ServerEnvelope.newBuilder()
                    .setActionType(ClientDataOuterClass.ActionType.START_LOCATION)
                    .setStartLocation(startLocationBuilder.build())
                    .build();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            envelope.writeDelimitedTo(baos);

            ByteBuf payload = Unpooled.wrappedBuffer(baos.toByteArray());
            channel.writeAndFlush(new BinaryWebSocketFrame(payload));
        }
    }

    public void removePlayer(GameEvent gameEvent) {
        if (ActionType.DISCONNECT.equals(gameEvent.getActionType())) {
            if (gameEvent.getChannel() != null) {
                this.world.removeBody(this.playersByChannelId.get(gameEvent.getChannel().id().asLongText()).getBody());
                this.playersByChannelId.remove(gameEvent.getChannel().id().asLongText());
            }
        }
    }

    private void broadcastGameState(long timestampMs) throws IOException {
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
            //TODO: Burada ilgili client'a gitme durumu eziliyor, dongunun son elemani tarafindan ezilmis oluyor yani
            ClientStateOuterClass.ClientState clientState = ClientStateOuterClass.ClientState.newBuilder()
                    .setX(pos.x)
                    .setY(pos.y)
                    .setLastProcessedSequenceId(p.getLastProcessedSequenceId())
                    .build();
            gs.setClientState(clientState);
        }

        GameStateOuterClass.GameState gameState = gs.build();

        server.ServerEnvelopeOuterClass.ServerEnvelope envelope =
                server.ServerEnvelopeOuterClass.ServerEnvelope.newBuilder()
                        .setActionType(ClientDataOuterClass.ActionType.GAME_STATE)
                        .setGameState(gameState)
                        .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        envelope.writeDelimitedTo(baos);
        ByteBuf payload = Unpooled.wrappedBuffer(baos.toByteArray());
        channels.writeAndFlush(new BinaryWebSocketFrame(payload));
    }

    @Override
    public void run() {
        double accumulator = 0.0;
        double t = 0.0;
        long lastTime = System.nanoTime();
        long simulationStartTimeMs = System.currentTimeMillis();
        while (true) {
            long now = System.nanoTime();
            double frameTime = (now - lastTime) / 1e9;
            lastTime = now;

            if (frameTime > 0.25) frameTime = 0.25;
            accumulator += frameTime;

            while (accumulator >= DT) {
                accumulator -= DT;
                t += DT;
                long simTimeMs = simulationStartTimeMs + (long) (t * 1000);

                Input in;
                while ((in = inputBuffer.peek()) != null
                        && in.getClientTimestampOffset() <= simTimeMs) {
                    inputBuffer.poll();
                    Player player = playersByChannelId.get(in.getChannelId());

                    if (player != null) {
                        Body body = player.getBody();
                        Vector2 currentPosition = body.getTransform().getTranslation();
                        Vector2 direction = new Vector2(in.getDx(), in.getDy());
                        if (!direction.isZero()) {
                            direction.normalize();
                        }

                        double speed = 5.0;
                        body.setLinearVelocity(direction.multiply(speed));
//                        Vector2 displacement = direction.multiply(speed);
//                        Vector2 newPosition = currentPosition.add(displacement);
//                        body.getTransform().setTranslation(newPosition);
                        //System.out.println(body.getTransform().getTranslation());

                        //set player's last processedSequenceId
                        player.setLastProcessedSequenceId(in.getSequenceId());
                    }
                }

                world.step(1, DT);

                try {
                    broadcastGameState(System.currentTimeMillis());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
