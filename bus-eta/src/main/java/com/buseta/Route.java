package com.buseta;

import java.util.ArrayList;
import java.util.List;


public class Route {

    private final int id;
    private final String name;
    private final List<Stop> stops = new ArrayList<>();

    public Route(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addStop(Stop stop) { stops.add(stop); }

    public int getId() { return id; }
    public String getName() { return name; }
    public List<Stop> getStops() { return stops; }


    public int indexOfStop(String stopName) {
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).getName().equalsIgnoreCase(stopName)) {
                return i;
            }
        }
        return -1;
    }


    public String toJson() {
        return "{\"route_id\":" + id + ",\"name\":\"" + name + "\"}";
    }
}
