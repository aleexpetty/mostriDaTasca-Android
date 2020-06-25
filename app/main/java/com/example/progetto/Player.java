package com.example.progetto;

public class Player {
    private String username, img, lifepoints, experience;

    public Player(String us, String image, String lp, String xp){
        username = us;
        img = image;
        lifepoints = lp;
        experience = xp;
    }

    public void setUsername(String user){
        this.username = user;
    }

    public void setImg(String image){
        this.img = image;
    }

    public void setLifepoints(String lp){
        this.lifepoints = lp;
    }

    public void setExperience(String xp){
        this.experience = xp;
    }

    public String getUsername(){
        return username;
    }

    public String getImg(){
        return img;
    }

    public String getLifepoints(){
        return lifepoints;
    }

    public String getExperience(){
        return experience;
    }

    public String getPlayer(){
        return "Giocatore: "+username+" "+img+" "+" Punti vita: "+lifepoints+" Punti esperienza: "+experience;
    }
}
