package com.component;

import com.artemis.Component;
import com.game.PlayerSnake;

public class PhysicBodyComponent extends Component {
    public PlayerSnake player;

    public PhysicBodyComponent() {}

    public PhysicBodyComponent(PlayerSnake player) {
        this.player = player;
    }
}