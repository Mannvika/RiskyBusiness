package com.example.demo;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class GameLogic
{
    // Map session id to player class containing players hand, investments, money, banked money, and other information
    // Also use it to s

    Map<String, Player> players;
    private final Map<String, Boolean> readyPlayers = new ConcurrentHashMap<>();
    Lobby lobby;
    GameWebSocketHandler webSocketHandler;
    private final Set<String> expectedPlayers;
    private final int NUM_ROUNDS = 10;

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
        ArrayList<String> testList = new ArrayList<String>();
        testList.add("Card 1");
        testList.add("Card 2");
        testList.add("Card 3");

        for(int round = 0; round < NUM_ROUNDS; round++) {
            System.out.println("Starting round " + round);
            if(round % 2 == 0) {
                for(Map.Entry<String, Player> playerEntry : players.entrySet()) {
                    String chosenCard = playerEntry.getValue().chooseCard(testList);
                    String message = playerEntry.getKey() + " chosen card: " + chosenCard;
                    String jsonMessage = String.format(
                            "{\"type\": \"message\", \"lobbyId\": \"%s\", \"message\": \"%s\"}",
                            lobby.getId(),
                            message.replace("\"", "\\\"")
                    );

                    webSocketHandler.sendToPlayer(playerEntry.getKey(), jsonMessage);
                }
            }

            resetReadyPlayers();

            // Wait for all players to be ready with a timeout
            long startTime = System.currentTimeMillis();
            while(!allPlayersReady()) {
                if (System.currentTimeMillis() - startTime > 30000) { // 30 second timeout
                    System.out.println("Timeout waiting for players to be ready");
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (allPlayersReady()) {
                System.out.println("All players ready, proceeding to next round");
                webSocketHandler.broadcastToLobby(lobby, "{\"type\": \"ROUND_COMPLETED\", \"round\": " + round + "}");
            }
        }
    }
}
