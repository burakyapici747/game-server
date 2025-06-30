package com.game;

import com.DrawingPanel;
import com.artemis.ComponentMapper;
import com.component.NettyChannelComponent;
import com.component.PhysicBodyComponent;
import com.event.GameEvent;
import com.event.data.Input;
import io.netty.channel.group.ChannelGroup;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.TimeStep;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;

public class Game implements Runnable {
    private final ComponentMapper<PhysicBodyComponent> physicBodyMapper;
    private final ComponentMapper<NettyChannelComponent> nettyChannelComponentMapper;

    private final double DT = 1 / 32.0;
    private final World<Body> gameWorld;
    private final com.artemis.World world;
    private final ChannelGroup channels;
    private final double SPEED = 10;
    private JFrame frame;
    private DrawingPanel drawingPanel;

    public final PriorityBlockingQueue<Input> inputBuffer = new PriorityBlockingQueue<>(
            11, Comparator.comparingLong(Input::getSequenceId)
    );

    private final Map<String, Integer> componentsByChannelId;

    public Game(ChannelGroup channels, com.artemis.World world, Map<String, Integer> componentsByChannelId) {
        this.physicBodyMapper = world.getMapper(PhysicBodyComponent.class);
        this.nettyChannelComponentMapper = world.getMapper(NettyChannelComponent.class);
        this.componentsByChannelId = componentsByChannelId;
        this.channels = channels;
        this.world = world;
        this.gameWorld = new World<>();
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
        if (input.getClientTimestampOffset() != null) {
            this.inputBuffer.offer(input);
        }
    }

    public void addPlayer(GameEvent gameEvent) {
        int componentId = componentsByChannelId.get(gameEvent.getChannel().id().asLongText());
        this.gameWorld.addBody(physicBodyMapper.get(componentId).body);
    }

    public void removePlayer(GameEvent gameEvent) {
        int componentId = componentsByChannelId.get(gameEvent.getChannel().id().asLongText());
        Body body = physicBodyMapper.get(componentId).body;
        this.gameWorld.removeBody(body);
    }

    @Override
    public void run() {
        double accumulator = 0.0;
        double t = 0.0;
        long lastTime = System.nanoTime();
        long simulationStartTimeMs = System.currentTimeMillis();
        TimeStep timeStep = new TimeStep(0.02);

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
                while ((in = inputBuffer.peek()) != null && in.getClientTimestampOffset() <= simTimeMs) {
                    inputBuffer.poll();
                    int componentId = componentsByChannelId.get(in.getChannelId());
                    Body body = physicBodyMapper.get(componentId).body;
                    double angleInRadians = Math.toRadians(in.getRotateAngle());
                    Vector2 direction = new Vector2(Math.cos(angleInRadians), Math.sin(angleInRadians));
                    body.setLinearVelocity(direction.multiply(SPEED));
                    body.integrateVelocity(World.ZERO_GRAVITY, timeStep, gameWorld.getSettings());
                    body.integratePosition(timeStep, gameWorld.getSettings());
                    nettyChannelComponentMapper.get(componentId).lastProcessedSequenceId = in.getSequenceId();
                }
                if (frameTime > 0.0) {
                    gameWorld.step(1, frameTime);
                }
                this.world.process();
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