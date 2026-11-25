package com.buseta;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class Simulator implements Runnable {

    private final List<Bus> buses;

    public Simulator(List<Bus> buses) {
        this.buses = buses;
    }

    @Override
    public void run() {
        while (true) {
            try (Connection conn = Database.connect()) {
                while (true) {
                    for (Bus bus : buses) {
                        bus.move();
                        Database.savePosition(conn, bus);
                    }
                    pause(Bus.TICK_SECONDS * 1000);
                }
            } catch (SQLException e) {

                System.out.println("Database problem, trying again in 5 seconds: " + e.getMessage());
                pause(5000);
            }
        }
    }

    private void pause(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }
}
