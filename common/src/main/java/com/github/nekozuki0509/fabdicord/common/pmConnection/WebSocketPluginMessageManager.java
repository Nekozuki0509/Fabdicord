package com.github.nekozuki0509.fabdicord.common.pmConnection;

public class WebSocketPluginMessageManager extends PluginMessageManager {

    private final WebSocketServer server;

    public WebSocketPluginMessageManager() {
        server = WebSocketServer.getInstance();
    }

    public void stop() {
        server.stop();
    }

    @Override
    public void sendMessage(String message) {
        server.sendMessage(message);
    }
}
