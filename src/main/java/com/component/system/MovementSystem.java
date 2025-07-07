package com.component.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.component.AngleComponent;
import com.component.PhysicBodyComponent;
import com.component.PositionComponent;
import com.component.VelocityComponent;

public class MovementSystem extends IteratingSystem {
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
        positionComponent.x = physicBodyComponent.player.getHead().getTransform().getTranslationX();
        positionComponent.y = physicBodyComponent.player.getHead().getTransform().getTranslationY();
        positionComponent.angle = Math.toDegrees(physicBodyComponent.player.getHead().getTransform().getRotationAngle());
        velocityComponent.dx = physicBodyComponent.player.getHead().getLinearVelocity().x;
        velocityComponent.dy = physicBodyComponent.player.getHead().getLinearVelocity().y;
    }
}