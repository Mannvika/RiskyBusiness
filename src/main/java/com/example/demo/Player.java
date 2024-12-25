package com.example.demo;

import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;

public class Player
{
    ArrayList<String> hand = new ArrayList<String>();

    Player()
    {

    }

    void chooseCard(String card)
    {
        hand.add(card);
    }

    void printHand()
    {
        for(String card : hand)
        {
            System.out.println(card);
        }
    }


}
