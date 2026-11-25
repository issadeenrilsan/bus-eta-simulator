package com.buseta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Database {


    private static final int SECONDS_BETWEEN_BUSES = 60;


    private static final String URL = setting("DB_URL", "jdbc:mysql://localhost:3306/bus_tracker_System");
    private static final String USER = setting("DB_USER", "root");
    private static final String PASSWORD = setting("DB_PASSWORD", "");

    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }


    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }


    public static List<Route> loadRoutes(Connection conn) throws SQLException {
        List<Route> routes = new ArrayList<>();


        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT route_id, Name FROM routes ORDER BY route_id")) {
            while (rs.next()) {
                routes.add(new Route(rs.getInt("route_id"), rs.getString("Name")));
            }
        }


        String stopSql = "SELECT Name, Lat, Lng FROM Stops WHERE route_id = ? ORDER BY stop_id";
        for (Route route : routes) {
            try (PreparedStatement ps = conn.prepareStatement(stopSql)) {
                ps.setInt(1, route.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        route.addStop(new Stop(rs.getString("Name"), rs.getDouble("Lat"), rs.getDouble("Lng")));
                    }
                }
            }
        }


        List<Route> usableRoutes = new ArrayList<>();
        for (Route route : routes) {
            if (route.getStops().size() >= 2) {
                usableRoutes.add(route);
            }
        }
        return usableRoutes;
    }


    public static List<Bus> loadBuses(Connection conn, List<Route> routes) throws SQLException {
        List<Bus> buses = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT bus_id, route_id, Name FROM Buses ORDER BY bus_id")) {
            while (rs.next()) {
                Route route = findRoute(routes, rs.getInt("route_id"));
                if (route == null) {
                    continue;
                }


                int busesAlreadyOnRoute = 0;
                for (Bus other : buses) {
                    if (other.getRouteId() == route.getId()) {
                        busesAlreadyOnRoute++;
                    }
                }
                int secondsUntilDeparture = busesAlreadyOnRoute * SECONDS_BETWEEN_BUSES;

                buses.add(new Bus(rs.getInt("bus_id"), rs.getString("Name"), route, secondsUntilDeparture));
            }
        }
        return buses;
    }

    private static Route findRoute(List<Route> routes, int routeId) {
        for (Route route : routes) {
            if (route.getId() == routeId) {
                return route;
            }
        }
        return null;
    }


    public static void savePosition(Connection conn, Bus bus) throws SQLException {
        String sql = "UPDATE Buses SET Current_lat = ?, Current_Lng = ? WHERE bus_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, bus.getLat());
            ps.setDouble(2, bus.getLng());
            ps.setInt(3, bus.getId());
            ps.executeUpdate();
        }
    }
}
