package com.example.demo;

import java.util.function.BiConsumer;

public class Investment
{
    public String name;
    public Player player;
    private final BiConsumer<Player, Integer> callback;

    public Investment(String name, BiConsumer<Player, Integer> callback)
    {
        this.name = name;
        this.callback = callback;
    }

    public void setPlayer (Player player)
    {
        this.player = player;
    }

    public Player getPlayer()
    {
        return player;
    }

    public void executeCallback(Integer round)
    {
        callback.accept(player, round);
    }
}
