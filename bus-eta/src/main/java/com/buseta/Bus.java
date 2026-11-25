package com.buseta;

import java.util.List;

public class Bus {


    public static final int TICK_SECONDS = 2;
    private static final double AVERAGE_SPEED_KMH = 40;
    private static final double SPEED_UP = 10;
    private static final double DEMO_SPEED_KMH = AVERAGE_SPEED_KMH * SPEED_UP;
    private static final double STEP_KM = DEMO_SPEED_KMH * TICK_SECONDS / 3600;
    private final int id;
    private final String name;
    private final Route route;

    private int secondsUntilDeparture;
    private int nextStopIndex = 1;
    private int direction = 1;
    private double lat;
    private double lng;

    public Bus(int id, String name, Route route, int secondsUntilDeparture) {
        this.id = id;
        this.name = name;
        this.route = route;
        this.secondsUntilDeparture = secondsUntilDeparture;

        Stop first = route.getStops().get(0);
        this.lat = first.getLat();
        this.lng = first.getLng();
    }


    public synchronized void move() {
        if (secondsUntilDeparture > 0) {
            secondsUntilDeparture = secondsUntilDeparture - TICK_SECONDS;
            return;
        }

        Stop target = route.getStops().get(nextStopIndex);
        double distanceLeft = Geo.distanceKm(lat, lng, target.getLat(), target.getLng());

        if (distanceLeft <= STEP_KM) {

            lat = target.getLat();
            lng = target.getLng();
            chooseNextStop();
        } else {

            double fraction = STEP_KM / distanceLeft;
            lat = lat + (target.getLat() - lat) * fraction;
            lng = lng + (target.getLng() - lng) * fraction;
        }
    }

    private void chooseNextStop() {
        int lastIndex = route.getStops().size() - 1;
        if (nextStopIndex == lastIndex) {
            direction = -1;   // reached the last stop, go back
        } else if (nextStopIndex == 0) {
            direction = 1;    // reached the first stop, go forward again
        }
        nextStopIndex = nextStopIndex + direction;
    }

    public synchronized boolean hasDeparted() {
        return secondsUntilDeparture <= 0;
    }

    public synchronized String toJson() {
        Stop next = route.getStops().get(nextStopIndex);
        double distance = Geo.distanceKm(lat, lng, next.getLat(), next.getLng());
        double etaMinutes = distance / DEMO_SPEED_KMH * 60;

        return "{\"bus_id\":" + id
                + ",\"bus_name\":\"" + name + "\""
                + ",\"next_stop\":\"" + next.getName() + "\""
                + ",\"distance_km\":" + round(distance, 2)
                + ",\"eta_minutes\":" + round(etaMinutes, 1) + "}";
    }


    public synchronized Arrival arrivalAt(String stopName) {
        if (secondsUntilDeparture > 0) {
            return null;
        }

        int stopIndex = route.indexOfStop(stopName);
        if (stopIndex < 0) {
            return null;
        }

        boolean stillComing = (direction == 1 && stopIndex >= nextStopIndex)
                || (direction == -1 && stopIndex <= nextStopIndex);
        if (!stillComing) {
            return null;
        }


        List<Stop> stops = route.getStops();
        Stop next = stops.get(nextStopIndex);
        double km = Geo.distanceKm(lat, lng, next.getLat(), next.getLng());
        int i = nextStopIndex;
        while (i != stopIndex) {
            Stop from = stops.get(i);
            Stop to = stops.get(i + direction);
            km = km + Geo.distanceKm(from.getLat(), from.getLng(), to.getLat(), to.getLng());
            i = i + direction;
        }

        double etaMinutes = km / DEMO_SPEED_KMH * 60;
        String goingTo = stops.get(direction == 1 ? stops.size() - 1 : 0).getName();

        String json = "{\"bus_name\":\"" + name + "\""
                + ",\"route\":\"" + route.getName() + "\""
                + ",\"going_to\":\"" + goingTo + "\""
                + ",\"distance_km\":" + round(km, 2)
                + ",\"eta_minutes\":" + round(etaMinutes, 1) + "}";
        return new Arrival(km, json);
    }

    private static double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    public int getId() { return id; }
    public int getRouteId() { return route.getId(); }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
}
