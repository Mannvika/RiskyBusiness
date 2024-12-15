package com.example.demo;

import java.util.HashSet;
import java.util.Set;

public class Lobby {
    private final String id;
    private final Set<String> players = new HashSet<>();
    private final Set<String> readyPlayers = new HashSet<>();

    public Lobby(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void addPlayer(String playerId) {
        players.add(playerId);
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
        readyPlayers.remove(playerId);
    }

    public void markPlayerReady(String playerId) {
        if (players.contains(playerId)) {
            readyPlayers.add(playerId);
        }
    }

    public boolean areAllPlayersReady() {
        for(String playerId : readyPlayers)
        {
            System.out.println(playerId);
        }
        return players.size() > 1 && players.equals(readyPlayers);
    }

    public Set<String> getPlayers() {
        return players;
    }
}
