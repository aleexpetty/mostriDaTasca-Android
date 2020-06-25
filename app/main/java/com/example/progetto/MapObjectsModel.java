package com.example.progetto;

import java.util.ArrayList;

public class MapObjectsModel {
    private static final MapObjectsModel ourInstance = new MapObjectsModel();
    private ArrayList<MapObject> mapObjects = null;

    public static MapObjectsModel getInstance() {
        return ourInstance;
    }

    private MapObjectsModel(){
        mapObjects = new ArrayList<MapObject>();
    }

    public void addMapObject(MapObject x){
        mapObjects.add(x);
    }

    public MapObject getMapObject(int index){
        return mapObjects.get(index);
    }

    public int getSize(){
        return mapObjects.size();
    }

    public void removeAll() { mapObjects.clear(); }

    public String toString() { return mapObjects.toString(); }

    public MapObject getMapObjectById(String id) {
        MapObject m = null;
        for(MapObject x : mapObjects){
            if(x.getId().equals(id))
                m=x;
        }
        return m;
    }

    public ArrayList<MapObject> getAll() {
        return mapObjects;
    }
}
