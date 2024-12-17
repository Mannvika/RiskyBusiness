package com.example.demo;

import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class GameLogic
{
    // Map session id to player class containing players hand, investments, money, banked money, and other information
    // Also use it to s

    Map<String, Player> players;
    Lobby lobby;
    GameWebSocketHandler webSocketHandler;
    private final int NUM_ROUNDS = 10;
    public GameLogic(Lobby lobby, GameWebSocketHandler webSocketHandler)
    {
        this.lobby = lobby;
        players = new ConcurrentHashMap<>();
        for(String player : lobby.getPlayers())
        {
            players.put(player, new Player());
        }
        this.webSocketHandler = webSocketHandler;
    }


    public void startGame()
    {
        ArrayList<String> testList = new ArrayList<String>();
        testList.add("Card 1");
        testList.add("Card 2");
        testList.add("Card 3");
        for(int round = 0; round < NUM_ROUNDS; round++)
        {
            if(round % 2 == 0)
            {
                for(Map.Entry<String, Player> playerEntry : players.entrySet())
                {
                    String chosenCard = playerEntry.getValue().chooseCard(testList);
                    String message = playerEntry.getKey() + " chosen card: " + chosenCard;

                    String jsonMessage = String.format(
                            "{\"type\": \"message\", \"lobbyId\": \"%s\", \"message\": \"%s\"}",
                            lobby.getId(),
                            message.replace("\"", "\\\"") // Escape double quotes in the message
                    );

                    webSocketHandler.broadcastToLobby(lobby, jsonMessage);

                }
            }
        }
    }
}
