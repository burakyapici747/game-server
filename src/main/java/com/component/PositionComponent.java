package com.component;

import com.artemis.Component;

public class PositionComponent extends Component {
    public double x;
    public double y;
    public double angle;

    public PositionComponent() {}

    public PositionComponent(double x, double y) {
        this.x = x;
        this.y = y;
        System.out.println("Calisti = " + x + " " + y);
    }
}