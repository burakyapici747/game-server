package com.game;

import com.DrawingPanel;
import com.Player;
import com.event.ActionType;
import com.event.GameEvent;
import com.event.data.Input;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.World;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;

public class Game {
    private final ScheduledExecutorService tickScheduler;

    private final long TICK_RATE_IN_MS = 16;
    private final World<Body> world;
    public final Map<String, Player> playersByChannelId = new ConcurrentHashMap<>();

    // Yeni: Görselleştirme için Swing bileşenleri
    private JFrame frame;
    private DrawingPanel drawingPanel;

    //TODO: Daha performansli bir veri yapisinda saklanacaklar!!!
    public final PriorityQueue<Input> inputBuffer = new PriorityQueue<>(
        Comparator.comparingLong(Input::getClientTimestampOffset)
    );
    //En son hangi timestamp degeri islendi, lastServerTickEndTimestamp + TICK_RATE_IN_MS degeri koyulacak
    public Long lastServerTickEndTimestamp = 0L;

    public Game() {
        //TODO: Initialize body count limit eklenebilir.
        this.world = new World<>();
        this.world.setGravity(World.ZERO_GRAVITY);
        //scheduler initialize
        //TODO: !!! onServerTick icerisindeki islemler TICK_RATE_IN_MS'DEN fazla surerse, görevler üst üste binebilir.
        this.tickScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r);
            //Arkaplan isi olarak calistirir
            t.setDaemon(false);
            return t;
        });
        this.tickScheduler.scheduleAtFixedRate(
            this::onServerTick,
            0L,
            TICK_RATE_IN_MS,
            TimeUnit.MILLISECONDS
        );

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
        }, 0L, TICK_RATE_IN_MS, TimeUnit.MILLISECONDS);
    }

    public void addInput(Input input) {
        if(input.getClientTimestampOffset() != null) {
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
            playerBody.addFixture(
                Geometry.createCircle(0.5),
                1.0,
                0.4, 0.4
            );
            playerBody.setMass(MassType.NORMAL);
            this.world.addBody(playerBody);

            return playerBody;
        }

        return null;
    }

    public void removePlayer(GameEvent gameEvent) {
        if (ActionType.DISCONNECT.equals(gameEvent.getActionType())) {
            if (gameEvent.getChannel() != null) {
                this.world.removeBody(this.playersByChannelId.get(gameEvent.getChannel().id().asLongText()).getBody());
            }
        }
    }

    public void onServerTick() {
        try {
            long tickStart = System.currentTimeMillis();
            double tickEnd = tickStart + TICK_RATE_IN_MS;

            List<Input> toProcess;

            calculatePhysics();
        }catch (Exception e) {
        }

    }

    private void calculatePhysics() {
        final double speed = 5;
        long startTime = System.nanoTime();
        while(!this.inputBuffer.isEmpty()) {
            Input in = this.inputBuffer.poll();
            Player player = playersByChannelId.get(in.getChannelId());
            if (player != null) {
                Body body = player.getBody();
                if (body != null) {
                    // --- Seçenek A: Sabit hız kullanmak (daha deterministik) ---
                    body.setLinearVelocity(in.getDx() * speed, in.getDy() * speed);

                    world.step(1, 0.016);
                    // --- Seçenek B: Kuvvet ile ilerle (ancak bu durumda önce kuvvet ve hızı sıfırlamak gerekebilir) ---
//                     body.setLinearVelocity(0, 0);
//                     body.applyForce(new Vector2(in.getDx() * forceMagnitude, in.getDy() * forceMagnitude));


                    //System.out.println("Pos: " + body.getTransform().getTranslation());

                }

            }
        }

        long endTime = System.nanoTime();

        long durationNs = (endTime - startTime);

        System.out.println(durationNs);

    }
}
