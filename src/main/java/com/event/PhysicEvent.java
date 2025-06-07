package com.event;

import com.event.data.Input;
import com.lmax.disruptor.EventHandler;

public class PhysicEvent implements EventHandler<GameEvent> {
    @Override
    public void onEvent(GameEvent gameEvent, long l, boolean b) throws Exception {
        if (ActionType.MOVE.equals(gameEvent.getActionType())) {

            Input input = new Input();

            input.setActionType(gameEvent.getActionType());
            input.setTimestamp(gameEvent.getTimestamp());
            input.setRotateAngle(gameEvent.getRotateAngle());
            input.setDx(gameEvent.getDx());
            input.setDy(gameEvent.getDy());
            input.setSequenceId(gameEvent.getSequenceId());
            input.setChannelId(gameEvent.getChannel().id().asLongText());
            input.setClientTimestampOffset(gameEvent.getClientTimestampOffset());

            long currentTime = System.currentTimeMillis();
            long currentTimeOffset = Math.abs(currentTime - gameEvent.getClientTimestampOffset());

            //System.out.println(currentTimeOffset);
//            System.out.println(gameEvent.getClientTimestampOffset() + " Sequence number" + gameEvent.getSequenceId() + " currentTimeoffset " + currentTimeOffset);

            gameEvent.getGame().addInput(input);
        }
    }
}
