package com.component.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.component.AngleComponent;
import com.component.PhysicBodyComponent;
import com.component.PositionComponent;
import com.component.VelocityComponent;

public class MovementSystem extends IteratingSystem {
    public static final short SPEED = 5;

    private ComponentMapper<PositionComponent> positionMapper;
    private ComponentMapper<VelocityComponent> velocityMapper;
    private ComponentMapper<PhysicBodyComponent> physicBodyMapper;

    public MovementSystem() {
        super(Aspect.all(PositionComponent.class, VelocityComponent.class, AngleComponent.class, PhysicBodyComponent.class));
    }

    @Override
    protected void initialize() {
        super.initialize();
        positionMapper = world.getMapper(PositionComponent.class);
        velocityMapper = world.getMapper(VelocityComponent.class);
        physicBodyMapper = world.getMapper(PhysicBodyComponent.class);
    }

    @Override
    protected void process(int id) {
        PositionComponent positionComponent = positionMapper.get(id);
        VelocityComponent velocityComponent = velocityMapper.get(id);
        PhysicBodyComponent physicBodyComponent = physicBodyMapper.get(id);
        positionComponent.x = physicBodyComponent.body.getTransform().getTranslationX();
        positionComponent.y = physicBodyComponent.body.getTransform().getTranslationY();
        velocityComponent.dx = physicBodyComponent.body.getLinearVelocity().x;
        velocityComponent.dy = physicBodyComponent.body.getLinearVelocity().y;
    }
}
