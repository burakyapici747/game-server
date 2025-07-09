package com.component.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.component.AngleComponent;
import com.component.PhysicBodyComponent;
import com.component.PositionComponent;
import com.component.VelocityComponent;

public class MovementSystem extends IteratingSystem {
    private ComponentMapper<AngleComponent> angleMapper;
    private ComponentMapper<PositionComponent> positionMapper;
    private ComponentMapper<VelocityComponent> velocityMapper;
    private ComponentMapper<PhysicBodyComponent> physicBodyMapper;

    public MovementSystem() {
        super(
            Aspect.all(
                PositionComponent.class,
                VelocityComponent.class,
                AngleComponent.class,
                PhysicBodyComponent.class
            )
        );
    }

    @Override
    protected void initialize() {
        super.initialize();
        angleMapper = world.getMapper(AngleComponent.class);
        positionMapper = world.getMapper(PositionComponent.class);
        velocityMapper = world.getMapper(VelocityComponent.class);
        physicBodyMapper = world.getMapper(PhysicBodyComponent.class);
    }

    @Override
    protected void process(int id) {
        PositionComponent positionComponent = positionMapper.get(id);
        VelocityComponent velocityComponent = velocityMapper.get(id);
        PhysicBodyComponent physicBodyComponent = physicBodyMapper.get(id);
        AngleComponent angleComponent = angleMapper.get(id);

        var headTransform = physicBodyComponent.player.getHead().getTransform();
        var headVelocity = physicBodyComponent.player.getHead().getLinearVelocity();

        positionComponent.x = physicBodyComponent.player.getHead().getTransform().getTranslationX();
        positionComponent.y = physicBodyComponent.player.getHead().getTransform().getTranslationY();


        angleComponent.angle = Math.toDegrees(headTransform.getRotationAngle());

        velocityComponent.dx = headVelocity.x;
        velocityComponent.dy = headVelocity.y;
    }
}
