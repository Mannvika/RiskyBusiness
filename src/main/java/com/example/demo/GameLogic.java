package com.example.demo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class GameLogic {
    private final Map<String, Player> players;
    private final Map<String, Boolean> readyPlayers = new ConcurrentHashMap<>();
    private final Lobby lobby;
    private final GameWebSocketHandler webSocketHandler;
    private final Set<String> expectedPlayers;
    private final int NUM_ROUNDS = 10;
    private final ExecutorService roundExecutor = Executors.newSingleThreadExecutor();

    private Map<String, ArrayList<Card>> lastGivenCards = new ConcurrentHashMap<>();
    public Map<String, Integer> lastChosenCards = new ConcurrentHashMap<>();
    private Map<String, ArrayList<String>> lastGivenInvestments = new ConcurrentHashMap<>();
    public Map<String, Integer> lastChosenInvestments = new ConcurrentHashMap<>();

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

    public synchronized void useCard(String playerId, int index)
    {
        Player callingPlayer = players.get(playerId);
        Player otherPlayer = null;
        for(String player : players.keySet())
        {
            if(playerId != player){
                otherPlayer = players.get(player);
                break;
            }
        }
        callingPlayer.useCard(index, otherPlayer);
    }

    public synchronized void chooseInvestments()
    {
        players.forEach((playerId, player) -> {
            player.chooseInvestment(lastGivenInvestments.get(playerId).get(lastChosenInvestments.get(playerId)));
            player.printHand();
        });

        lastGivenInvestments = new ConcurrentHashMap<>();
        lastChosenInvestments = new ConcurrentHashMap<>();
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

    public void broadcastGameState() {
        players.forEach((playerId, player) -> {
            StringBuilder jsonBuilder = new StringBuilder("{");
            jsonBuilder.append("\"type\": \"ROUND_END\",");
            jsonBuilder.append("\"lobbyId\": \"").append(lobby.getId()).append("\",");
            jsonBuilder.append("\"onHandCash\": ").append(player.getOnHandCash()).append(",");
            jsonBuilder.append("\"bankedCash\": ").append(player.getBankedCash()).append(",");
            jsonBuilder.append("\"hand\": [");

            List<Card> hand = player.getHand();
            for (int i = 0; i < hand.size(); i++) {
                jsonBuilder.append("\"").append(hand.get(i).name.replace("\"", "\\\"")).append("\"");
                if (i < hand.size() - 1) {
                    jsonBuilder.append(",");
                }
            }

            jsonBuilder.append("],");
            jsonBuilder.append("\"investments\": [");

            List<String> investments = player.getInvestments();
            for (int i = 0; i < investments.size(); i++) {
                jsonBuilder.append("\"").append(investments.get(i).replace("\"", "\\\"")).append("\"");
                if (i < investments.size() - 1) {
                    jsonBuilder.append(",");
                }
            }

            jsonBuilder.append("]");
            jsonBuilder.append("}");

            String jsonMessage = jsonBuilder.toString();
            System.out.println(jsonMessage);
            webSocketHandler.sendToPlayer(playerId, jsonMessage);
        });
    }


    public void startGame() {
        System.out.println("Starting game with " + expectedPlayers.size() + " players");

        ArrayList<Card> testList = new ArrayList<>();
        testList.add(new Card("Reduce by 15%", player -> {player.onHandCash -= (int) (player.onHandCash * 0.15);}));
        testList.add(new Card("Increment by 15%", player -> {player.onHandCash += (int) (player.onHandCash * 0.15);}));
        testList.add(new Card("Subtract by 100",  player -> {player.onHandCash -= 100;}));

        ArrayList<String> testInvestment = new ArrayList<>();
        testInvestment.add("Inv 1");
        testInvestment.add("Inv 2");
        testInvestment.add("Inv 3");

        roundExecutor.submit(() -> {
            for (int round = 0; round < NUM_ROUNDS; round++) {
                System.out.println("Starting round " + round);

                if (round == 0 || round == 3 || round == 7) {
                    players.forEach((playerId, player) -> {
                        StringBuilder jsonBuilder = new StringBuilder("{");
                        jsonBuilder.append("\"type\": \"PICK_INVESTMENT\",");
                        jsonBuilder.append("\"lobbyId\": \"").append(lobby.getId()).append("\",");
                        jsonBuilder.append("\"investments\": [");

                        for (int i = 0; i < testInvestment.size(); i++) {
                            jsonBuilder.append("\"").append(testInvestment.get(i).replace("\"", "\\\"")).append("\"");
                            if (i < testInvestment.size() - 1) {
                                jsonBuilder.append(",");
                            }
                        }

                        jsonBuilder.append("]}");

                        String jsonMessage = jsonBuilder.toString();
                        lastGivenInvestments.put(playerId, testInvestment);
                        webSocketHandler.sendToPlayer(playerId, jsonMessage);
                    });
                }

                // Card Selection
                if (round == 0 || round == 4) {
                    players.forEach((playerId, player) -> {
                        StringBuilder jsonBuilder = new StringBuilder("{");
                        jsonBuilder.append("\"type\": \"PICK_CARD\",");
                        jsonBuilder.append("\"lobbyId\": \"").append(lobby.getId()).append("\",");
                        jsonBuilder.append("\"cards\": [");

                        for (int i = 0; i < testList.size(); i++) {
                            jsonBuilder.append("\"").append(testList.get(i).name.replace("\"", "\\\"")).append("\"");
                            if (i < testList.size() - 1) {
                                jsonBuilder.append(",");
                            }
                        }

                        jsonBuilder.append("]}");

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

                if(round == 0 || round == 4) {chooseCards();}
                if(round == 0 || round == 3 || round == 7) {chooseInvestments();}
                System.out.println("Round " + round);
                broadcastGameState();
                resetReadyPlayers();
            }
        });
    }
}
