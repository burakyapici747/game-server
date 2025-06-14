package com.util;

import client.ClientDataOuterClass;
import com.event.ActionType;
import com.event.GameEvent;
import io.netty.channel.Channel;

public final class GameEventMapper {
    private GameEventMapper() {
    }

    public static void toGameEvent(ClientDataOuterClass.ClientData clientData, GameEvent target, Channel channel) {
        if (clientData != null && target != null) {
            target.setActionType(convertProtoActionTypeToLocalActionType(clientData.getActionType()));
            target.setChannel(channel);
            target.setDx(clientData.getDx());
            target.setDy(clientData.getDy());
            target.setSequenceId(clientData.getSequenceId());
            target.setClientTimestampOffset(clientData.getTimestamp());
            target.setDeltaTime(clientData.getDeltaTime());
        }
    }

    private static ActionType convertProtoActionTypeToLocalActionType(ClientDataOuterClass.ActionType protoActionType) {
        return ActionType.valueOf(protoActionType.name());
    }
}
