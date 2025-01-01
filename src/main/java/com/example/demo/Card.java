package com.example.demo;
import java.util.function.Consumer;

class Card {
    public String name;
    private final Consumer<Player> callback;

    public Card(String name, Consumer<Player> callback)
    {
        this.name = name;
        this.callback = callback;
    }

    public void executeCallback(Player player)
    {
        callback.accept(player);
    }
}
