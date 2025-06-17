package com;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import java.awt.*;

public class DebugDraw {
    protected World<Body> world;
    protected double scale = 100.0; // 1 birim = 100 piksel

    public DebugDraw(World<Body> world) {
        this.world = world;
    }

    public void render(Graphics2D g) {
        g.setColor(Color.RED);
        for (Body body : this.world.getBodies()) {
            if (body.getFixture(0).getShape() instanceof Circle) {
                double x = body.getTransform().getTranslationX();
                double y = body.getTransform().getTranslationY();
                double radius = ((Circle) (body.getFixture(0).getShape())).getRadius();
                int px = (int) ((x - radius) * scale);
                int py = (int) ((y - radius) * scale);
                int diameter = (int) (radius * 2 * scale);
                g.fillOval(px, py, diameter, diameter);
                g.drawString("[" + body.getTransform().getTranslationX() + "," + body.getTransform().getTranslationY() + "]", px, py);
            }
        }
    }
}