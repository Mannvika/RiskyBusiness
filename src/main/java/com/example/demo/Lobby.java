package com.example.demo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Lobby {
    private final String id;
    private final HashMap<String, String> players = new HashMap<>();
    private final Set<String> readyPlayers = new HashSet<>();

    public Lobby(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void addPlayer(String playerId, String playerName) {
        players.put(playerId, playerName);
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        readyPlayers.remove(playerId);
    }

    public void markPlayerReady(String playerId) {
        if (players.containsKey(playerId)) {
            readyPlayers.add(playerId);
        }
    }

    public boolean areAllPlayersReady() {
        for (String playerId : players.keySet()) {
            if (!readyPlayers.contains(playerId)) {
                return false; // If any player is not ready, return false
            }
        }
        return players.size() > 1; // All players are ready if there is more than 1 player
    }

    public HashMap<String, String> getPlayers() {
        return players;
    }
}
