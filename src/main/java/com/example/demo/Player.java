package com.example.demo;

import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;

public class Player
{
    ArrayList<Card> hand = new ArrayList<>();
    ArrayList<Investment> investments = new ArrayList<>(4);

    public int bankedCash = 0;
    public int onHandCash = 1000;
    public float investmentMultiplier = 1;

    String name = "";

    Player(String name)
    {
        this.name = name;
    }

    int getOnHandCash(){return onHandCash;}
    int getBankedCash(){return bankedCash;}

    public ArrayList<Card> getHand() {
        return hand;
    }

    public ArrayList<Investment> getInvestments() {
        return investments;
    }

    void chooseCard(Card card)
    {
        hand.add(card);
    }

    Card useCard(int index, Player otherPlayer)
    {
        Card card = hand.get(index);
        card.executeCallback(this, otherPlayer);
        hand.remove(card);
        return card;
    }

    void useInvestments(int round)
    {
        for(Investment investment : investments)
        {
            investment.executeCallback(round);
        }
    }

    void chooseInvestment(Investment investment)
    {
        investments.add(investment);
        investment.setPlayer(this);
    }
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
