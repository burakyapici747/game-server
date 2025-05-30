package com.event;

import com.lmax.disruptor.EventHandler;

public class PhysicEvent implements EventHandler<GameEvent> {
    @Override
    public void onEvent(GameEvent gameEvent, long l, boolean b) throws Exception {

    }
}
