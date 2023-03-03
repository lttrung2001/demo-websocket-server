package com.example.demo.configs;

import com.example.demo.models.Payload;
import com.google.gson.Gson;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyWebSocketHandler extends AbstractWebSocketHandler {
    private final Map<String, List<WebSocketSession>> topics = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        List<WebSocketSession> sessions = topics.get("global");
        if (sessions == null) {
            sessions = new ArrayList<>();
        }
        sessions.add(session);
        topics.put("global", sessions);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        List<WebSocketSession> sessions = topics.get("global");
        sessions.remove(session);
        System.out.println("Number of connections: " + sessions.size());
        if (sessions.size() > 0) {
            sessions.forEach(s -> {
                try {
                    s.sendMessage(new TextMessage(session.getId() + "has been disconnected."));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Payload payload = new Gson().fromJson(message.getPayload(), Payload.class);
        String msg = payload.getMessage();

        System.out.println(msg);
        for (WebSocketSession s : topics.get("global")) {
            s.sendMessage(new TextMessage(session.getId() + ": " + msg));
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        Thread.sleep(200);
        super.handleBinaryMessage(session, message);
        ByteBuffer byteBuffer = message.getPayload();
        System.out.println(byteBuffer);
        for (WebSocketSession s : topics.get("global")) {
            s.sendMessage(message);
        }
    }
}
