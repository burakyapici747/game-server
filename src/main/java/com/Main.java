package com;

import com.server.WebsocketServer;

public class Main {
    public static void main(String[] args) throws Exception {
        WebsocketServer websocketServer = new WebsocketServer();
        websocketServer.startServer();
    }
}