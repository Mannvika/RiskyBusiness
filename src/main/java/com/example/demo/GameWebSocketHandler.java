package com.example.demo;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    public GameLogic gameLogic = new GameLogic(sessions);
    private final GameService gameService;

    public GameWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("Connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        Map<String, String> parsedMessage = parseMessage(payload);

        String type = parsedMessage.get("type");
        String lobbyId = parsedMessage.get("lobbyId");
        String playerId = session.getId();

        System.out.println("Received: " + type + " " + lobbyId + " " + playerId);

        if ("JOIN_LOBBY".equals(type))
        {
            Lobby lobby = gameService.getLobby(lobbyId);
            if (lobby == null)
            {
                lobby = gameService.createLobby(lobbyId);
            }
            lobby.addPlayer(playerId);
        }
        else if ("READY".equals(type))
        {
            gameService.markPlayerReady(lobbyId, playerId);
            Lobby lobby = gameService.getLobby(lobbyId);
            if (lobby != null)
            {
                if (gameService.areAllPlayersReady(lobbyId))
                {
                    broadcastToLobby(lobby, "{\"type\": \"GAME_STARTING\"}");
                    // start game
                }
                else
                {
                    broadcastToLobby(lobby, "{\"type\": \"WAITING\"}");
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        System.out.println("Disconnected: " + session.getId());
    }

    private Map<String, String> parseMessage(String payload) {
        Map<String, String> result = new ConcurrentHashMap<>();
        String[] pairs = payload.replaceAll("[{}\"]", "").split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                result.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return result;
    }

    private void broadcastToLobby(Lobby lobby, String message) {
        lobby.getPlayers().forEach(playerId -> {
            WebSocketSession session = sessions.get(playerId);
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
