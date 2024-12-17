package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<String, Lobby> lobbies = new ConcurrentHashMap<>();
    private final Map<String, GameLogic> gameLogics = new ConcurrentHashMap<>();

    public Lobby createLobby(String lobbyId) {
        Lobby lobby = new Lobby(lobbyId);
        lobbies.put(lobbyId, lobby);
        return lobby;
    }

    public void startGame(String lobbyId, GameWebSocketHandler webSocketHandler) {
        Lobby lobby = lobbies.get(lobbyId);
        if (lobby != null) {
            GameLogic gameLogic = new GameLogic(lobby, webSocketHandler);
            gameLogics.put(lobbyId, gameLogic);
            gameLogic.startGame();
        }
    }

    public Lobby getLobby(String lobbyId) {
        return lobbies.get(lobbyId);
    }

    public GameLogic getGameLogic(String lobbyId)
    {
        return gameLogics.get(lobbyId);
    }

    public void removeLobby(String lobbyId) {
        lobbies.remove(lobbyId);
    }

    public void markPlayerReady(String lobbyId, String playerId) {
        Lobby lobby = lobbies.get(lobbyId);
        if (lobby != null) {
            lobby.markPlayerReady(playerId);
        }
    }

    public boolean areAllPlayersReady(String lobbyId) {
        Lobby lobby = lobbies.get(lobbyId);
        return lobby != null && lobby.areAllPlayersReady();
    }
}
