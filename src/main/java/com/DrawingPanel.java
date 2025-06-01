package com;

import org.dyn4j.dynamics.Body;
import org.dyn4j.world.World;

import javax.swing.*;
import java.awt.*;

public class DrawingPanel extends JPanel {
    private final World<Body> world;
    private final DebugDraw debugDraw;

    public DrawingPanel(World<Body> world) {
        this.world = world;
        this.debugDraw = new DebugDraw(world);
        this.setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Anti-aliasing açalım
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Çizimi gerçekleştir
        debugDraw.render(g2d);
    }
}
