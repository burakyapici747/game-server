package com.game;

import client.ClientDataOuterClass;
import com.DrawingPanel;
import com.artemis.ComponentMapper;
import com.component.NettyChannelComponent;
import com.component.PhysicBodyComponent;
import com.event.GameEvent;
import com.event.data.Input;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;
import server.StartInformationOuterClass;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Game implements Runnable {
    private final ComponentMapper<PhysicBodyComponent> physicBodyMapper;
    private final ComponentMapper<NettyChannelComponent> nettyChannelComponentMapper;

    private final double DT = 1 / 64.0;
    private final World<Body> gameWorld;
    private final com.artemis.World world;
    private final ChannelGroup channels;
    private final double SPEED = 10;
    private final double ROTATION_SPEED = 1.75;
    private JFrame frame;
    private DrawingPanel drawingPanel;


    public final Queue<Input> inputBuffer = new ArrayDeque<>();

    private final Map<String, Integer> componentsByChannelId;

    public Game(ChannelGroup channels, com.artemis.World world, Map<String, Integer> componentsByChannelId) {
        this.physicBodyMapper = world.getMapper(PhysicBodyComponent.class);
        this.nettyChannelComponentMapper = world.getMapper(NettyChannelComponent.class);
        this.componentsByChannelId = componentsByChannelId;
        this.channels = channels;
        this.world = world;
        this.gameWorld = new World<>();
        //this.gameWorld.addContactListener(new )
        this.gameWorld.getSettings().setMaximumTranslation(100);
        //TODO: Collision yapisi kuruldugunda bu asagidaki 2'si aktiflestirilip test edilebilir
        //this.gameWorld.getSettings().setVelocityConstraintSolverIterations(8);
        //this.gameWorld.getSettings().setPositionConstraintSolverIterations(3);
        this.gameWorld.setGravity(World.ZERO_GRAVITY);

        SwingUtilities.invokeLater(this::initializeVisualization);
    }

    private void initializeVisualization() {
        this.frame = new JFrame("Game Physics Viewer");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.drawingPanel = new DrawingPanel(this.gameWorld);
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
        this.inputBuffer.offer(input);
    }

    public void addPlayer(GameEvent gameEvent, StartInformationDto startInformationDto) {
        int entityId = componentsByChannelId.get(gameEvent.getChannel().id().asLongText());
        PlayerSnake playerSnake = physicBodyMapper.get(entityId).player;
        this.gameWorld.addBody(playerSnake.getHead());
        playerSnake.segments.forEach(this.gameWorld::addBody);
        sendStartInformation(gameEvent.getChannel(), startInformationDto);
    }

    public void removePlayer(GameEvent gameEvent) {
        int entityId = componentsByChannelId.get(gameEvent.getChannel().id().asLongText());
        PlayerSnake playerSnake = physicBodyMapper.get(entityId).player;
        this.gameWorld.removeBody(playerSnake.getHead());
        playerSnake.segments.forEach(this.gameWorld::removeBody);
    }

    public void sendStartInformation(Channel channel, StartInformationDto startInformationDto) {
        server.ServerEnvelopeOuterClass.ServerEnvelope.Builder serverEnvelopeBuilder =
            server.ServerEnvelopeOuterClass.ServerEnvelope.newBuilder();

        StartInformationOuterClass.StartInformation startInformation = StartInformationOuterClass.StartInformation.newBuilder()
            .setStartDirection(startInformationDto.playerStartDirection())
            .setX(startInformationDto.startPosition().x)
            .setY(startInformationDto.startPosition().y)
            .setSegmentCount(startInformationDto.segmentCount())
            .build();

        server.ServerEnvelopeOuterClass.ServerEnvelope serverEnvelope = serverEnvelopeBuilder
            .setActionType(ClientDataOuterClass.ActionType.START_INFORMATION)
            .setStartInformation(startInformation)
            .build();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            serverEnvelope.writeDelimitedTo(baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ByteBuf payload = Unpooled.wrappedBuffer(baos.toByteArray());

        channel.writeAndFlush(new BinaryWebSocketFrame(payload));
    }


    @Override
    public void run() {
        double accumulator = 0.0;
        long lastTime = System.nanoTime();

        while (true) {
            long now = System.nanoTime();
            double frameTime = (now - lastTime) / 1e9;
            lastTime = now;

            if (frameTime > 0.25) frameTime = 0.25;

            accumulator += frameTime;
            while (accumulator >= DT) {
                accumulator -= DT;

                Input in;
                while ((in = inputBuffer.peek()) != null) {
                    int componentId = componentsByChannelId.get(in.getChannelId());
                    PlayerSnake playerSnake = physicBodyMapper.get(componentId).player;
                    Body body = playerSnake.getHead();

                    double currentAngle = body.getTransform().getRotationAngle();
                    double targetAngle = Math.toRadians(in.getRotateAngle() + 90);
                    double angleDifference = targetAngle - currentAngle;

                    angleDifference = Math.atan2(Math.sin(angleDifference), Math.cos(angleDifference));

                    double newSmoothedAngle = currentAngle + (angleDifference * ROTATION_SPEED * 0.02);

                    Vector2 direction = new Vector2(Math.cos(newSmoothedAngle), Math.sin(newSmoothedAngle));

                    body.setLinearVelocity(direction.multiply(SPEED));
                    body.getTransform().setRotation(newSmoothedAngle);
                    inputBuffer.poll();
                }
                gameWorld.step(1, DT);
                this.world.process();
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}