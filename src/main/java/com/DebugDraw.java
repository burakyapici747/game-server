package com;

import org.dyn4j.dynamics.Body;
import org.dyn4j.world.World;

import java.awt.*;

public class DebugDraw {
    protected World<Body> world;
    protected double scale = 50.0; // 1 birim = 50 piksel

    public DebugDraw(World<Body> world) {
        this.world = world;
    }

    public void render(Graphics2D g) {
        // Basit bir örnek: her bodynin sabit daire şeklindeki fixture'ını çizelim
        g.setColor(Color.RED);
        for (Body body : this.world.getBodies()) {
            // Pozisyon ve boyutu alın
            double x = body.getTransform().getTranslationX();
            double y = body.getTransform().getTranslationY();
            // Burada sadece ilk fixture'ı daire olarak çiziyoruz:
            double radius = 0.5; // örneğin
            int px = (int) ((x - radius) * scale);
            int py = (int) ((y - radius) * scale);
            int diameter = (int)(radius * 2 * scale);
            // Y ekseni için ters çevirme (ayarlanabilir)
            g.fillOval(px, py, diameter, diameter);
        }
    }
}