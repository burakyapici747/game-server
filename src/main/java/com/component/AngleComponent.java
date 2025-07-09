package com.component;

import com.artemis.Component;

public class AngleComponent extends Component {
    public double angle;

    public AngleComponent() {}

    public AngleComponent(double angle){
        System.out.println("Calisti = " + angle);
        this.angle = angle;
    }
}
