package com.game;

import org.dyn4j.geometry.Vector2;
import server.StartInformationOuterClass;

public record StartInformationDto(
        PlayerSnake playerSnake,
        StartInformationOuterClass.StartDirection playerStartDirection,
        Vector2 startPosition,
        int segmentCount
) {
}