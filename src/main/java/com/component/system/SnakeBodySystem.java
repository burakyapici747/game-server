package com.component.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.component.PhysicBodyComponent;
import com.game.PlayerSnake;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

public class SnakeBodySystem extends IteratingSystem {

    private final double MAX_ANGLE_DIFFERENCE = Math.toRadians(10);
    private static final double SEGMENT_DISTANCE = 0.3;

    protected ComponentMapper<PhysicBodyComponent> physicBodyMapper;

    public SnakeBodySystem() {
        super(Aspect.all(PhysicBodyComponent.class));
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.physicBodyMapper = world.getMapper(PhysicBodyComponent.class);
    }

    @Override
    protected void process(int entityId) {
        PlayerSnake playerSnake = physicBodyMapper.get(entityId).player;
        Transform parentTransform = playerSnake.getHead().getTransform();

        for (Body segmentBody : playerSnake.getSegments()) {
            Transform currentTransform = segmentBody.getTransform();
            Vector2 parentPosition = parentTransform.getTranslation();
            Vector2 currentPosition = currentTransform.getTranslation();

            // 1. İstenen Yön ve Açı: Parçanın, ebeveyne bakması gereken ideal açı.
            Vector2 directionToParent = parentPosition.difference(currentPosition);
            // atan2, yön vektörünün açısını radyan cinsinden verir.
            double desiredAngle = Math.atan2(directionToParent.y, directionToParent.x);

            // 2. Ebeveynin Açısını Al
            double parentAngle = parentTransform.getRotationAngle();

            // 3. Açıyı Kısıtla (Clamp)
            // İki açı arasındaki en kısa farkı bul (-PI ile +PI arasında)
            double angleDiff = parentAngle - desiredAngle;
            angleDiff = Math.atan2(Math.sin(angleDiff), Math.cos(angleDiff));

            double finalAngle;
            if (Math.abs(angleDiff) > MAX_ANGLE_DIFFERENCE) {
                // Fark, limitten büyükse, açıyı maksimuma kelepçele
                desiredAngle = parentAngle - (Math.signum(angleDiff) * MAX_ANGLE_DIFFERENCE);
            }
            finalAngle = desiredAngle;

            // 4. Yeni Rotasyonu ve Pozisyonu Uygula
            currentTransform.setRotation(finalAngle);

            // Yeni pozisyonu, ebeveynin pozisyonundan, bu yeni rotasyonun tersi yönünde
            // segmentDistance kadar uzağa ayarla. Bu, client ile birebir aynı mantıktır.
            Vector2 offsetDirection = new Vector2(Math.cos(finalAngle), Math.sin(finalAngle));
            Vector2 newPosition = parentPosition.difference(offsetDirection.multiply(SEGMENT_DISTANCE));

            currentTransform.setTranslation(newPosition);

            // Bu segmentin transformu, bir sonraki segmentin ebeveyni (hedefi) olur.
            parentTransform = currentTransform;
        }
    }
}