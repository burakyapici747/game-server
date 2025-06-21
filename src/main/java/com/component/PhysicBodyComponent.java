package com.component;

import com.artemis.Component;
import org.dyn4j.dynamics.Body;

public class PhysicBodyComponent extends Component {
    public Body body;

    public PhysicBodyComponent() {}

    public PhysicBodyComponent(Body body) {
        this.body = body;
    }
}
