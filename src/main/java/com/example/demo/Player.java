package com.example.demo;

import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;

public class Player
{
    ArrayList<Card> hand = new ArrayList<>();
    ArrayList<Investment> investments = new ArrayList<>(4);

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

    public ArrayList<Investment> getInvestments() {
        return investments;
    }

    void chooseCard(Card card)
    {
        hand.add(card);
    }

    void useCard(int index, Player otherPlayer)
    {
        hand.get(index).executeCallback(this, otherPlayer);
        hand.remove(hand.get(index));
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
