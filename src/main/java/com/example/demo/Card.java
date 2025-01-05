package com.example.demo;
import java.util.function.BiConsumer;

class Card {
    public String name;
    private final BiConsumer<Player, Player> callback;

    public Card(String name, BiConsumer<Player, Player> callback)
    {
        this.name = name;
        this.callback = callback;
    }

    public void executeCallback(Player player, Player otherPlayer)
    {
        callback.accept(player, otherPlayer);
    }
}
