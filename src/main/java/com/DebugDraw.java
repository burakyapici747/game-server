package com;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Circle;
import org.dyn4j.world.World;

import java.awt.*;

public class DebugDraw {
    protected World<Body> world;
    protected double scale = 25.0; // 1 birim = 100 piksel

    public DebugDraw(World<Body> world) {
        this.world = world;
    }

    public void render(Graphics2D g) {
        g.setColor(Color.RED);
        for (Body body : this.world.getBodies()) {
            if (body.getFixture(0).getShape() instanceof Circle) {
                // Dünya koordinatları
                double x = body.getTransform().getTranslationX();
                double y = body.getTransform().getTranslationY();
                double radius = ((Circle)body.getFixture(0).getShape()).getRadius();

                // Ölçekli ekran koordinatları (merkez nokta)
                double centerScreenX = x * scale;
                double centerScreenY = y * scale;

                // Ekrandaki sol-üst köşe koordinatları
                int px = (int)(centerScreenX - radius * scale);
                // Y eksenini ters çevir
                int py = 1080 - (int)(centerScreenY + radius * scale);

                int diameter = (int)(radius * 2 * scale);

                g.fillOval(px, py, diameter, diameter);
                // Metin de aynı tersleme ile
                String posText = String.format("[%.2f, %.2f]", x, y);
                //g.drawString(posText, px, py);
            }
        }
    }
}