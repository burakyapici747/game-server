package com.game;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import server.StartInformationOuterClass;

import java.util.LinkedList;
import java.util.Random;

public class PlayerBuilder {
    private final Vector2 startPosition = new Vector2(5, 5);
    private static final Random random = new Random();
    private static final double SEGMENT_DISTANCE = 0.3;

    public StartInformationDto createPlayer(int segmentCount, double radius, int entityId) {
        Body head = new Body();
        LinkedList<Body> segments = new LinkedList<>();
        PlayerSnake playerSnake = new PlayerSnake(entityId, head, segments);
        head.addFixture(
                Geometry.createCircle(0.4),
                1.0,
                0.0,
                0.0
        );
        head.setMass(MassType.NORMAL);
        head.setEnabled(true);
        head.getTransform().setTranslation(new Vector2(5, 5));
        head.setAtRestDetectionEnabled(false);
        //TODO: Zamanla yavaşlamaması için bir ayar 0 yada 1 olacak kontrol et.
        head.setLinearDamping(0);

        //TODO: Random'dan gelen degere gore direction ayarlanacak
        Vector2 direction2 = new Vector2(Math.cos(Math.toRadians(90)), Math.sin(Math.toRadians(90)));

        head.setLinearVelocity(direction2.multiply(10));
        head.setUserData(entityId);

        StartInformationOuterClass.StartDirection[] directions = StartInformationOuterClass.StartDirection.values();
        // UNRECOGNIZED'ı hariç tutmak için -1
        StartInformationOuterClass.StartDirection randomDirection = directions[random.nextInt(directions.length - 1)];

        Vector2 offset = new Vector2();
        switch (randomDirection) {
            case UP:
                offset.set(0, -1);
                break; // Yukarı giderken vücut aşağıda
            case DOWN:
                offset.set(0, 1);
                break; // Aşağı giderken vücut yukarıda
            case LEFT:
                offset.set(1, 0);
                break; // Sola giderken vücut sağda
            case RIGHT:
                offset.set(-1, 0);
                break; // Sağa giderken vücut solda
        }

        Vector2 lastPosition = startPosition.copy();
        for (int i = 0; i < segmentCount; i++) {
            Body segmentBody = new Body();
            BodyFixture fixture = segmentBody.addFixture(new Circle(0.3));
            fixture.setSensor(true);
            segmentBody.setMass(MassType.INFINITE);
            segmentBody.setUserData(entityId);
            // Yeni segment pozisyonunu ofsete göre hesapla
            Vector2 segmentPosition = lastPosition.add(offset.copy().multiply(SEGMENT_DISTANCE));
            segmentBody.getTransform().setTranslation(segmentPosition);
            segments.add(segmentBody);
            lastPosition = segmentPosition;
        }
        return new StartInformationDto(playerSnake, randomDirection, startPosition, segmentCount);
    }
}