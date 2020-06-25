package com.example.progetto;
import android.content.Context;

public class ProfileModel {
    private static ProfileModel instance;
    private static Context context;
    private String sessionID, username, image, lp, xp;

    private ProfileModel(Context ctx){
        context = ctx;
    }

    public static synchronized ProfileModel getInstance(Context ctx) {
        if (instance == null) {
            instance = new ProfileModel(ctx);
        }
        return instance;
    }

    public void setProfile(String s, String u, String i, String l, String x){
        sessionID = s;
        username = u;
        image = i;
        lp = l;
        xp = x;
    }

    @Override
    public String toString() {
        return  "sessionID: " + sessionID +
                ", username: " + username +
                ", image: " + image +
                ", LP: " + lp +
                ", XP:" + xp;
    }

    public void setSessionID(String s){
        sessionID = s;
    }

    public void setUsername(String s){
        username = s;
    }

    public void setImage(String s){
        image = s;
    }

    public void setLp(String lp) {
        this.lp = lp;
    }

    public void setXp(String xp) {
        this.xp = xp;
    }

    public String getSessionID(){
        return sessionID;
    }

    public String getUsername(){
        return username;
    }

    public String getImage(){
        return image;
    }

    public String getLp() {
        return lp;
    }

    public String getXp() {
        return xp;
    }
}
