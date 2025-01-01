package com.example.demo;

import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;

public class Player
{
    ArrayList<Card> hand = new ArrayList<>();
    ArrayList<String> investments = new ArrayList<>(4);

    public int bankedCash = 0;
    public int onHandCash = 1000;

    Player()
    {

    }

    int getOnHandCash(){return onHandCash;}
    int getBankedCash(){return bankedCash;}

    public ArrayList<Card> getHand() {
        return hand;
    }

    public ArrayList<String> getInvestments() {
        return investments;
    }

    void chooseCard(Card card)
    {
        hand.add(card);
    }

    void useCard(int index, Player player)
    {
        hand.get(index).executeCallback(player);
        hand.remove(hand.get(index));
    }

    void chooseInvestment(String card){ investments.add(card); }
    void bankCash(int cash)
    {
        bankedCash += cash;
        onHandCash -= cash;
    }

    void printHand()
    {
        for(Card card : hand)
        {
            System.out.println(card.name);
        }
    }
}
