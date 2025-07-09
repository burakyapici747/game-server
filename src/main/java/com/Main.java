package com;

import com.server.WebSocketServer;

public class Main {
    public static void main(String[] args) throws Exception {
        WebSocketServer websocketServer = new WebSocketServer();
        websocketServer.start();
    }
}