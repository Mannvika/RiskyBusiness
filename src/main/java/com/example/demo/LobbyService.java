package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LobbyService {

    private final Map<String, List<String>> lobbies = new HashMap<>();

    public String createLobby() {
        String lobbyId = UUID.randomUUID().toString();
        lobbies.put(lobbyId, new ArrayList<>());
        return lobbyId;
    }

    public boolean joinLobby(String lobbyId, String playerName) {
        if (lobbies.containsKey(lobbyId)) {
            lobbies.get(lobbyId).add(playerName);
            return true;
        }
        return false;
    }

    public List<String> getPlayers(String lobbyId) {
        return lobbies.getOrDefault(lobbyId, null);
    }

    public Map<String, List<String>> getAllLobbies() {
        return lobbies;
    }

    public boolean removePlayer(String lobbyId, String playerName) {
        if (lobbies.containsKey(lobbyId)) {
            return lobbies.get(lobbyId).remove(playerName);
        }
        return false;
    }

    public boolean deleteLobby(String lobbyId) {
        return lobbies.remove(lobbyId) != null;
    }
}
