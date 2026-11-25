package com.buseta;


public class Arrival {

    private final double distanceKm;
    private final String json;

    public Arrival(double distanceKm, String json) {
        this.distanceKm = distanceKm;
        this.json = json;
    }

    public double getDistanceKm() { return distanceKm; }
    public String toJson() { return json; }
}
