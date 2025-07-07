package com.game;

import org.dyn4j.dynamics.Body;

import java.util.LinkedList;

public class PlayerSnake {
    private final int entityId;
    private final Body head;
    public final LinkedList<Body> segments;

    public PlayerSnake(int entityId, Body head, LinkedList<Body> segments) {
        this.entityId = entityId;
        this.head = head;
        this.segments = segments;
    }

    public int getEntityId() {
        return entityId;
    }

    public Body getHead() {
        return head;
    }

    public LinkedList<Body> getSegments() {
        return segments;
    }
}