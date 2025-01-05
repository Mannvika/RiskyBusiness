package com.example.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ChoiceGenerator {
    // Static fields for global access
    private static final ArrayList<Card> cards = new ArrayList<>();
    private static final ArrayList<Investment> investments = new ArrayList<>();

    static {
        // Initialize static fields in a static block
        cards.add(new Card("Reduce by 15%", (player, otherPlayer) -> otherPlayer.onHandCash -= (int) (otherPlayer.onHandCash * 0.15)));
        cards.add(new Card("Increment by 15%", (player, otherPlayer) -> player.onHandCash += (int) (player.onHandCash * 0.15)));
        cards.add(new Card("Subtract by 100", (player, otherPlayer) -> otherPlayer.onHandCash -= 100));
        cards.add(new Card("Increment multiplier by 50%", (player, otherPlayer) -> player.investmentMultiplier += 0.5f));
        cards.add(new Card("Reduce multiplier by 25%", (player, otherPlayer) -> otherPlayer.investmentMultiplier -= 0.25f));


        investments.add(new Investment("Late Game", (player, round) -> {
            if (round < 6) {
                player.onHandCash += (int) ((player.onHandCash * 0.15) * player.investmentMultiplier);
            } else {
                player.onHandCash += (int) ((player.onHandCash * 0.40) * player.investmentMultiplier);
            }
        }));

        investments.add(new Investment("Mid Game", (player, round) -> {
            if (round < 4) {
                player.onHandCash += (int) ((player.onHandCash * 0.25) * player.investmentMultiplier);
            } else {
                player.onHandCash += (int) ((player.onHandCash * 0.375) * player.investmentMultiplier);
            }
        }));

        investments.add(new Investment("Early Game", (player, round) -> {
            if (round > 5) {
                player.onHandCash += (int) ((player.onHandCash * 0.20) * player.investmentMultiplier);
            } else {
                player.onHandCash += (int) ((player.onHandCash * 0.8) * player.investmentMultiplier);
            }
        }));
    }

    // Generate a list of unique cards
    public static ArrayList<Card> generateUniqueCards(int n) {
        if (n > cards.size()) {
            throw new IllegalArgumentException("n cannot be greater than the number of available cards");
        }

        ArrayList<Card> shuffledCards = new ArrayList<>(cards);
        Collections.shuffle(shuffledCards);
        return new ArrayList<>(shuffledCards.subList(0, n));
    }

    // Generate a list of unique investments
    public static ArrayList<Investment> generateUniqueInvestments(int n) {
        if (n > investments.size()) {
            throw new IllegalArgumentException("n cannot be greater than the number of available investments");
        }

        ArrayList<Investment> shuffledInvestments = new ArrayList<>(investments);
        Collections.shuffle(shuffledInvestments);
        return new ArrayList<>(shuffledInvestments.subList(0, n));
    }
}
