package com.event;

import com.event.data.Input;
import com.lmax.disruptor.EventHandler;

public class PhysicEvent implements EventHandler<GameEvent> {
    @Override
    public void onEvent(GameEvent gameEvent, long l, boolean b) throws Exception {
        if (ActionType.MOVE.equals(gameEvent.getActionType())) {
            Input input = new Input();
            input.setActionType(gameEvent.getActionType());
            input.setRotateAngle(gameEvent.getRotateAngle());
            input.setChannelId(gameEvent.getChannel().id().asLongText());
            gameEvent.getGame().addInput(input);
        }
    }
}
