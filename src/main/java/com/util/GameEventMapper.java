package com.util;

import com.event.GameEvent;
import com.event.SmallGameEvent;

public final class GameEventMapper {
    private GameEventMapper() {
    }

    public static void toGameEvent(SmallGameEvent smallGameEvent, GameEvent target) {
        if (smallGameEvent != null && target != null) {
            target.setActionType(smallGameEvent.getActionType());
            target.setChannel(smallGameEvent.getChannel());
            target.setDx(smallGameEvent.getDx());
            target.setDy(smallGameEvent.getDy());
            target.setTimestamp(smallGameEvent.getTimestamp());
            target.setSequenceId(smallGameEvent.getSequenceId());
            target.setRotateAngle(smallGameEvent.getRotateAngle());
            target.setClientTimestampOffset(smallGameEvent.getClientTimestampOffset());
        }
    }
}
