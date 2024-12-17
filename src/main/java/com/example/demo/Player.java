package com.example.demo;

import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;

public class Player
{
    ArrayList<String> hand = new ArrayList<String>();

    Player()
    {

    }

    String chooseCard(ArrayList<String> cards)
    {
        int rand = (int)(Math.random()*cards.size());
        hand.add(cards.get(rand));
        return cards.get(rand);
    }


}
