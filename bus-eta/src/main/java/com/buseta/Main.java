package com.buseta;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class Main {

    public static void main(String[] args) throws Exception {


        List<Route> routes;
        List<Bus> buses;
        try (Connection conn = Database.connect()) {
            routes = Database.loadRoutes(conn);
            buses = Database.loadBuses(conn, routes);
        } catch (SQLException e) {
            System.out.println("Could not read from the database: " + e.getMessage());
            System.out.println("Check that MySQL is running and the settings in Database.java are right.");
            return;
        }
        System.out.println("Loaded " + routes.size() + " route(s) and " + buses.size() + " bus(es).");


        Thread simulatorThread = new Thread(new Simulator(buses));
        simulatorThread.setDaemon(true);
        simulatorThread.start();


        String portText = System.getenv("PORT");
        int port = (portText == null) ? 8081 : Integer.parseInt(portText);
        new WebServer(routes, buses).start(port);
    }
}
