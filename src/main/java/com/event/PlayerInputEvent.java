package com.event;

import com.lmax.disruptor.EventHandler;

public class PlayerInputEvent implements EventHandler<GameEvent> {
    @Override
    public void onEvent(GameEvent gameEvent, long l, boolean b) throws Exception {
        //TODO: Burada aykiri degerler temizlenebilir!!!
        if(ActionType.MOVE.equals(gameEvent.getActionType())) {
            //
        }
    }
}
