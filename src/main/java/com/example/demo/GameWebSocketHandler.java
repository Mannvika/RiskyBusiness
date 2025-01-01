package com.example.demo;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameWebSocketHandler extends TextWebSocketHandler {

    public final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final GameService gameService;
    private final LobbyService lobbyService;

    public GameWebSocketHandler(LobbyService lobbyService, GameService gameService) {
        this.gameService = gameService;
        this.lobbyService = lobbyService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String sessionId = session.getId();
        System.out.println("=== Detailed Message Info ===");
        System.out.println("Session ID: " + sessionId);
        System.out.println("Message: " + message.getPayload());
        System.out.println("Current Sessions: " + sessions.keySet());
        System.out.println("==========================");

        String payload = message.getPayload();
        Map<String, String> parsedMessage = parseMessage(payload);

        String type = parsedMessage.get("type");
        String playerName = parsedMessage.get("playerName");
        String lobbyId = parsedMessage.get("lobbyId");
        String index = parsedMessage.get("index");
        String playerId = sessionId;

        if ("CREATE_LOBBY".equals(type)) {
            try {
                String newLobbyId = lobbyService.createLobby();
                gameService.createLobby(newLobbyId);

                String response = String.format(
                        "{\"type\":\"LOBBY_CREATED\",\"lobbyId\":\"%s\",\"playerName\":\"%s\"}",
                        newLobbyId,
                        playerName
                );

                sendToPlayer(playerId, response);
            } catch (Exception e) {
                sendToPlayer(playerId, "{\"type\":\"ERROR\",\"message\":\"Failed to create lobby\"}");
                e.printStackTrace();
            }
        }
        else if ("JOIN_LOBBY".equals(type)) {
            try {
                Lobby lobby = gameService.getLobby(lobbyId);
                lobby.addPlayer(playerId);
                System.out.println("Player " + playerId + " joined lobby " + lobbyId);

                String response = String.format(
                        "{\"type\":\"LOBBY_JOINED\",\"lobbyId\":\"%s\",\"playerId\":\"%s\",\"playerName\":\"%s\"}",
                        lobbyId,
                        playerId,
                        playerName
                );

                sendToPlayer(playerId, response);
            } catch (Exception e) {
                sendToPlayer(playerId, "{\"type\":\"ERROR\",\"message\":\"Failed to join lobby\"}");
                e.printStackTrace();
            }
        }
        else if ("READY".equals(type)) {
            try {
                gameService.markPlayerReady(lobbyId, playerId);
                Lobby lobby = gameService.getLobby(lobbyId);

                if(lobby == null) {System.out.println("lobby is null");}

                if (lobby != null && gameService.areAllPlayersReady(lobbyId)) {
                    System.out.println("All players ready in lobby " + lobbyId);
                    System.out.println("Active sessions before game start: " + sessions.keySet());

                    String gameStartingResponse = "{\"type\":\"GAME_STARTING\",\"lobbyId\":\"" + lobbyId + "\"}";
                    broadcastToLobby(lobby, gameStartingResponse);
                    gameService.startGame(lobbyId, this);
                }
                else
                {
                    String response = String.format(
                            "{\"type\":\"PLAYER_READY\",\"lobbyId\":\"%s\",\"playerId\":\"%s\"}",
                            lobbyId,
                            playerId
                    );
                    sendToPlayer(playerId, response);
                }
            } catch (Exception e) {
                sendToPlayer(playerId, "{\"type\":\"ERROR\",\"message\":\"Failed to mark player ready\"}");
                e.printStackTrace();
            }
        }
        else if ("ROUND_READY".equals(type)) {
            try {
                System.out.println("ROUND_READY from session " + sessionId);
                System.out.println("Current lobby state: " + gameService.getLobby(lobbyId).getPlayers());

                GameLogic gameLogic = gameService.getGameLogic(lobbyId);
                if (gameLogic != null) {
                    gameLogic.markRoundReady(playerId);

                    String response = String.format(
                            "{\"type\":\"ROUND_READY_CONFIRMED\",\"lobbyId\":\"%s\",\"playerId\":\"%s\"}",
                            lobbyId,
                            playerId
                    );

                    sendToPlayer(playerId, response);
                } else {
                    System.out.println("Warning: No GameLogic found for lobby " + lobbyId);
                    sendToPlayer(playerId, "{\"type\":\"ERROR\",\"message\":\"Game logic not found\"}");
                }
            } catch (Exception e) {
                sendToPlayer(playerId, "{\"type\":\"ERROR\",\"message\":\"Failed to process round ready\"}");
                e.printStackTrace();
            }
        }
        else if("CHOOSE_CARD".equals(type))
        {
            try
            {
                System.out.println("CHOOSE_CARD from session " + sessionId);
                System.out.println("Current lobby state: " + gameService.getLobby(lobbyId).getPlayers());

                GameLogic gameLogic = gameService.getGameLogic(lobbyId);
                gameLogic.lastChosenCards.put(playerId, Integer.parseInt(index));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        else if("CHOOSE_INVESTMENT".equals(type))
        {
            try
            {
                System.out.println("CHOOSE_INVESTMENT from session " + sessionId);
                System.out.println("Current lobby state: " + gameService.getLobby(lobbyId).getPlayers());

                GameLogic gameLogic = gameService.getGameLogic(lobbyId);
                gameLogic.lastChosenInvestments.put(playerId, Integer.parseInt(index));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else if("USE_CARD".equals(type))
        {
            try
            {
                System.out.println("USE_CARD from session " + sessionId);
                System.out.println("Current lobby state: " + gameService.getLobby(lobbyId).getPlayers());

                GameLogic gameLogic = gameService.getGameLogic(lobbyId);
                gameLogic.useCard(playerId, Integer.parseInt(index));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("New connection established: " + session.getId());
        System.out.println("Current active sessions: " + sessions.keySet());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        System.out.println("Connection closed: " + sessionId);
        System.out.println("Remaining sessions: " + sessions.keySet());
    }

    private Map<String, String> parseMessage(String payload) {
        Map<String, String> result = new ConcurrentHashMap<>();

        // Remove the curly braces and quotes
        payload = payload.replaceAll("[{}\"]", "");

        // Split into key-value pairs
        String[] pairs = payload.split(",");

        for (String pair : pairs) {
            // Split each pair into key and value
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                result.put(key, value);
            }
        }

        return result;
    }

    public void broadcastToLobby(Lobby lobby, String message) {
        if (lobby == null || message == null) {
            System.err.println("Invalid parameters: lobby or message is null.");
            return;
        }

        System.out.println("Broadcasting message to lobby " + message);
        lobby.getPlayers().forEach(playerId -> sendToPlayer(playerId, message));
    }

    public void sendToPlayer(String playerId, String message) {
        if (playerId == null || message == null) {
            // Replace with a logging framework
            System.err.println("Invalid parameters: playerId or message is null.");
            return;
        }

        WebSocketSession session = sessions.get(playerId);

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                // Replace with a logging framework
                System.err.println("Error sending message to player: " + playerId);
                e.printStackTrace();
            }
        } else {
            // Replace with a logging framework
            System.err.println("Session is null or closed for player: " + playerId);
        }
    }
}
