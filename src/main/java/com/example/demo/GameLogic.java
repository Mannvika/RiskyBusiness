package com.example.demo;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;

public class GameLogic
{
    // Map session id to player class containing players hand, investments, money, banked money, and other information
    // Also use it to s

    Map<String, Player> players;
    private final Map<String, Boolean> readyPlayers = new ConcurrentHashMap<>();
    Lobby lobby;
    GameWebSocketHandler webSocketHandler;
    private final Set<String> expectedPlayers;
    private final int NUM_ROUNDS = 2;

    public GameLogic(Lobby lobby, GameWebSocketHandler webSocketHandler) {
        this.lobby = lobby;
        this.webSocketHandler = webSocketHandler;
        this.players = new ConcurrentHashMap<>();
        this.expectedPlayers = new HashSet<>(lobby.getPlayers());
        for(String player : lobby.getPlayers()) {
            players.put(player, new Player());
        }
    }

    public synchronized void markRoundReady(String player) {
        System.out.println("Marking player " + player + " as ready for round");
        readyPlayers.put(player, true);
        System.out.println("Current ready players: " + readyPlayers.size() + "/" + expectedPlayers.size());
    }

    public synchronized boolean allPlayersReady() {
        boolean allReady = expectedPlayers.size() == readyPlayers.size() &&
                expectedPlayers.containsAll(readyPlayers.keySet());
        System.out.println("Checking if all players ready: " + allReady +
                " (Ready: " + readyPlayers.size() +
                ", Expected: " + expectedPlayers.size() + ")");
        return allReady;
    }

    private synchronized void resetReadyPlayers() {
        readyPlayers.clear();
        System.out.println("Ready players have been reset. Expecting " + expectedPlayers.size() + " players");
    }

    public void startGame() {
        System.out.println("Starting game with " + expectedPlayers.size() + " players");
        ArrayList<String> testList = new ArrayList<>();
        testList.add("Card 1");
        testList.add("Card 2");
        testList.add("Card 3");

        for (int round = 0; round < NUM_ROUNDS; round++) {
            int currentRound = round; // Make round effectively final for lambda
            System.out.println("Starting round " + currentRound);

            if (currentRound % 2 == 0) {
                players.forEach((playerId, player) -> {
                    String chosenCard = player.chooseCard(testList);
                    String message = playerId + " chosen card: " + chosenCard;
                    String jsonMessage = String.format(
                            "{\"type\": \"message\", \"lobbyId\": \"%s\", \"message\": \"%s\"}",
                            lobby.getId(),
                            message.replace("\"", "\\\"")
                    );

                    webSocketHandler.sendToPlayer(playerId, jsonMessage);
                });
            }

            // Handle waiting asynchronously
            CompletableFuture.runAsync(() -> {
                long startTime = System.currentTimeMillis();
                while (!allPlayersReady()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                if (allPlayersReady()) {
                    System.out.println("All players ready, proceeding to next round");
                    webSocketHandler.broadcastToLobby(lobby, "{\"type\": \"ROUND_COMPLETED\", \"round\": " + currentRound + "}");
                    resetReadyPlayers();
                }
            });
        }
    }
}
