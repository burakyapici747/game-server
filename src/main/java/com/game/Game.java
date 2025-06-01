package com.game;

import com.DrawingPanel;
import com.Player;
import com.event.ActionType;
import com.event.GameEvent;
import com.event.data.Input;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Game {
    private final ScheduledExecutorService tickScheduler;

    private final Long TICK_RATE_IN_MS = 100L;
    private final World<Body> world;
    public final Map<String, Player> playersByChannelId = new ConcurrentHashMap<>();

    // Yeni: Görselleştirme için Swing bileşenleri
    private JFrame frame;
    private DrawingPanel drawingPanel;

    //TODO: Daha performansli bir veri yapisinda saklanacaklar!!!
    public final List<Input> inputBuffer = new ArrayList<>();
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
        synchronized (this.inputBuffer) {
            this.inputBuffer.add(input);
        }
    }

    public Body addPlayer(GameEvent gameEvent) {
        Body playerBody = new Body();

        if (ActionType.CONNECT.equals(gameEvent.getActionType())) {
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
            long tickEnd = tickStart + TICK_RATE_IN_MS;

            List<Input> toProcess;
            synchronized (this.inputBuffer) {
                // 1) “T’den önceki” input’ları temizleme
                Iterator<Input> iterator = this.inputBuffer.iterator();
//                while (iterator.hasNext()) {
//                    Input inp = iterator.next();
//                    if (inp.getTimestamp() < tickStart) {
//                        // Bu input’u T’den önce geldiği için artık kullanmayacağız
//                        iterator.remove();
//                    } else {
//                        // Birazdan bu listeyi filtreleyeceğiz, ama
//                        // kesinlikle T’den küçük olanlar artık orada kalmasın.
//                    }
//                }

                // 2) T … T+TICK_RATE aralığındaki input’ları ayıklama
//                for (Input inp : this.inputBuffer) {
//                    long ts = inp.getTimestamp();
//                    if (ts >= tickStart && ts < tickEnd) {
//                        toProcess.add(inp);
//                    }
//                }
                toProcess = new ArrayList<>(this.inputBuffer);
            }
            toProcess.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
            calculatePhysics(toProcess, tickStart);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private void calculatePhysics(List<Input> inputs, long tickStart) {

        double simTimeMs = 0.0;
        final double speed = 5.0; // Birim zamanda (örneğin saniyede) birim piksel/ölçü. İstersen ayarla.

        // 1) Gelen input’ları zaman sırasına göre sıraladık (zaten caller’da da sort edebilirsin).
        inputs.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

        for (Input in : inputs) {
            // 2a) Bu input’un “tick başlangıcından ne kadar ms sonra” gerçekleştiğini hesapla
            double eventTimeMs = (double)(in.getTimestamp() - tickStart);
            // 2b) simTimeMs’tan eventTimeMs’e kadar geçen süre dtMs
            double dtMs = eventTimeMs - simTimeMs;
            if (dtMs < 0) {
                // Bazen aynı timestamp veya küçük timestamp gelmiş olabilir, bu durumda
                // dtMs’yi 0 kabul et ve devam et.
                dtMs = 0;
            }
            double dtSec = dtMs / 1000.0;

            // 2c) O ana kadar fizik motorunu ilerlet
            if (dtSec > 0) {
                world.step(1, dtSec);
                simTimeMs += dtMs; // “Zamanı tüket”
            }

            // 3) Şimdi ilgili oyuncu için dx/dy’ye göre Body’ye atamayı yap
            Player player = playersByChannelId.get(in.getChannelId());
            if (player != null) {
                Body body = player.getBody();
                if (body != null) {
                    // --- Seçenek A: Sabit hız kullanmak (daha deterministik) ---
                    body.setLinearVelocity(in.getDx() * speed, in.getDy() * speed);

                    // --- Seçenek B: Kuvvet ile ilerle (ancak bu durumda önce kuvvet ve hızı sıfırlamak gerekebilir) ---
                    // body.setLinearVelocity(0, 0);
                    // body.applyForce(new Vector2(in.getDx() * forceMagnitude, in.getDy() * forceMagnitude));
                }
            }
            // Döngü başa döndüğünde, bir sonraki input için simTimeMs doğru tutulacak.
        }

        // 4) Böylece tüm input’lar işlendi. Şimdi “o tick süresi” (örneğin 100ms) dolana kadar
        //    kalan süre için de fizik adımını bir kez daha uygula.
        double remainingTimeMs = TICK_RATE_IN_MS - simTimeMs;
        if (remainingTimeMs < 0) {
            remainingTimeMs = 0;
        }
        double remainingTimeSec = remainingTimeMs / 1000.0;
        if (remainingTimeSec > 0) {
            world.step(1, remainingTimeSec);
        }
    }
}
