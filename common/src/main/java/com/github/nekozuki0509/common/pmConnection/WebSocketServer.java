package com.github.nekozuki0509.common.pmConnection;

import com.github.nekozuki0509.common.Common;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.glassfish.tyrus.server.Server;

@ServerEndpoint(
        value = "/pm",
        configurator = WebSocketServer.SingletonConfigurator.class
)
public class WebSocketServer {

    private static WebSocketServer INSTANCE;

    private volatile Session session;

    private volatile Server server;

    private volatile boolean serverStarted = false;

    public WebSocketServer() {
    }

    public static class SingletonConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) {
            return endpointClass.cast(WebSocketServer.getInstance());
        }
    }

    private void startServer() {
        new Thread(() -> {
            while (!serverStarted) {
                if (Common.getServer() == null) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Common.getLOGGER().error("WebSocketサーバースレッドが割り込まれました: {}", ExceptionUtils.getStackTrace(e));
                    }
                    continue;
                }

                String ip = Common.getServer().getServerIp() == null || Common.getServer().getServerIp().isEmpty() ? "0.0.0.0" : Common.getServer().getServerIp();
                int port = Common.getServer().getServerPort() + Common.getWebSocketPortIncrement();
                try {
                    server = new Server(ip, port, "/ws", null, WebSocketServer.class);
                    server.start();
                    Common.getLOGGER().info("WebSocketサーバー起動: ws://{}:{}/ws/pm", ip, port);
                    serverStarted = true;

                    break;
                } catch (Exception e) {
                    server = null;
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Common.getLOGGER().error("WebSocketサーバースレッドが割り込まれました: {}", ExceptionUtils.getStackTrace(ex));
                        return;
                    }
                }
            }
        }, "websocket-server").start();
    }

    public void stop() {
        if (server != null) {
            server.stop();
            server = null;
            Common.getLOGGER().info("WebSocketサーバー停止");
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        Common.getLOGGER().info("WebSocket connection established.");
    }

    @OnMessage
    public void onMessage(String message, Session sender) {
        PluginMessageManager.receive("%s&%s".formatted(Common.getServerName(), message).split("&"));
    }

    @OnError
    public void onError(Session session, Throwable e) {
        Common.getLOGGER().error("WebSocket error occurred.: {}", ExceptionUtils.getStackTrace(e));
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        Common.getLOGGER().info("WebSocket connection closed.: {}", closeReason);
        this.session = null;
    }

    public void sendMessage(String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            Common.getLOGGER().error("WebSocketメッセージ送信失敗: {}", ExceptionUtils.getStackTrace(e));
        }
    }

    public static WebSocketServer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WebSocketServer();
            INSTANCE.startServer();
        }

        return INSTANCE;
    }
}
