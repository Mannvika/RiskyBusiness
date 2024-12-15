package com.example.demo;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameLogic
{
    // Map session id to player class containing players hand, investments, money, banked money, and other information
    // Also use it to s

    //Map<String, Player> sessions;
    private final int NUM_ROUNDS = 10;
    public GameLogic(Map<String, WebSocketSession> sessions)
    {
        // For each string in session, add it to the player map with a new player
        // Player class should contain reference to WebSocketSession
    }

    public void startGame()
    {
        for(int round = 0; round < NUM_ROUNDS; round++)
        {
            if(round % 2 == 0)
            {
                // For each player in player map call choose card
                // pass in array containing three random cards
                // choose card function should update player instance with selected card and then return the name
                // of that card to be broadcasted in a json to front end.
            }
        }
    }
}
