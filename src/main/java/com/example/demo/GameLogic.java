package com.example.demo;

import javax.smartcardio.Card;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class GameLogic {
    private final Map<String, Player> players;
    private final Map<String, Boolean> readyPlayers = new ConcurrentHashMap<>();
    private final Lobby lobby;
    private final GameWebSocketHandler webSocketHandler;
    private final Set<String> expectedPlayers;
    private final int NUM_ROUNDS = 5;
    private final ExecutorService roundExecutor = Executors.newSingleThreadExecutor();

    private  Map<String, ArrayList<String>> lastGivenCards = new ConcurrentHashMap<>();
    public  Map<String, Integer> lastChosenCards = new ConcurrentHashMap<>();

    public GameLogic(Lobby lobby, GameWebSocketHandler webSocketHandler) {
        this.lobby = lobby;
        this.webSocketHandler = webSocketHandler;
        this.players = new ConcurrentHashMap<>();
        this.expectedPlayers = new HashSet<>(lobby.getPlayers());
        for (String player : lobby.getPlayers()) {
            players.put(player, new Player());
        }
    }

    public synchronized void chooseCards()
    {
        players.forEach((playerId, player) -> {
            player.chooseCard(lastGivenCards.get(playerId).get(lastChosenCards.get(playerId)));
            player.printHand();
        });

        lastGivenCards = new ConcurrentHashMap<>();
        lastChosenCards = new ConcurrentHashMap<>();
    }

    public synchronized void markRoundReady(String player) {
        System.out.println("Marking player " + player + " as ready for round");
        readyPlayers.put(player, true);
        System.out.println("Current ready players: " + readyPlayers.size() + "/" + expectedPlayers.size());
    }

    public synchronized boolean allPlayersReady() {
        boolean allReady = expectedPlayers.size() == readyPlayers.size() &&
                expectedPlayers.containsAll(readyPlayers.keySet());
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

        roundExecutor.submit(() -> {
            for (int round = 0; round < NUM_ROUNDS; round++) {
                System.out.println("Starting round " + round);

                if (round % 2 == 0) {
                    players.forEach((playerId, player) -> {
                        StringBuilder jsonBuilder = new StringBuilder("{");
                        jsonBuilder.append("\"type\": \"PICK_CARD\",");
                        jsonBuilder.append("\"lobbyId\": \"").append(lobby.getId()).append("\",");

                        for (int i = 0; i < testList.size(); i++) {
                            jsonBuilder.append("\"card").append(i + 1).append("\": \"")
                                    .append(testList.get(i).replace("\"", "\\\"")).append("\"");
                            if (i < testList.size() - 1) {
                                jsonBuilder.append(",");
                            }
                        }

                        jsonBuilder.append("}");

                        String jsonMessage = jsonBuilder.toString();
                        lastGivenCards.put(playerId, testList);
                        webSocketHandler.sendToPlayer(playerId, jsonMessage);
                    });
                }

                long startTime = System.currentTimeMillis();
                while (!allPlayersReady()) {
                    try {
                        Thread.sleep(100);
                        if (System.currentTimeMillis() - startTime > 120000) { // 120 seconds timeout
                            System.err.println("Timeout waiting for players to be ready");
                            break;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Thread interrupted while waiting for players");
                        return;
                    }
                    //System.out.println("Waiting for players to be ready");
                }

                System.out.println("All players ready, proceeding to next round");

                if(round % 2 == 0) {chooseCards();}
                webSocketHandler.broadcastToLobby(lobby, "{\"type\": \"ROUND_COMPLETED\", \"round\": " + round + "}");
                resetReadyPlayers();
            }
        });
    }
}
