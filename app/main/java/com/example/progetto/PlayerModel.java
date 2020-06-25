package com.example.progetto;

import java.util.ArrayList;

public class PlayerModel {
    private static PlayerModel ourInstance;
    private ArrayList<Player> players = null;

    public static synchronized PlayerModel getInstance() {
        if (ourInstance == null) {
            ourInstance = new PlayerModel();
        }
        return ourInstance;
    }
    private PlayerModel(){
        players = new ArrayList<Player>();
    }

    public void addPlayer(Player p){
        players.add(p);
    }

    public Player getPlayer(int index){
        return players.get(index);
    }

    public int getSize(){
        return players.size();
    }

    public void removeAll(){
        players.clear();
    }
}