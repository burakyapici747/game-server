package com;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    private final Map<String, Player> activePlayerMap = new HashMap<>();
    private final Map<String, Channel> channelMap = new HashMap<>();
}
