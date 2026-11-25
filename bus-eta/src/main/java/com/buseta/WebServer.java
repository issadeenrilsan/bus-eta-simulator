package com.buseta;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class WebServer {

    private final List<Route> routes;
    private final List<Bus> buses;

    public WebServer(List<Route> routes, List<Bus> buses) {
        this.routes = routes;
        this.buses = buses;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // When the browser asks for this address, run this method.
        server.createContext("/", exchange -> sendHome(exchange));
        server.createContext("/api/routes", exchange -> sendRoutes(exchange));
        server.createContext("/api/stops", exchange -> sendStops(exchange));
        server.createContext("/api/arrivals", exchange -> sendArrivals(exchange));
        server.createContext("/api/buses", exchange -> sendBuses(exchange));

        server.start();
        if (WebServer.class.getResource("/web/index.html") == null
                || WebServer.class.getResource("/web/style.css") == null
                || WebServer.class.getResource("/web/app.js") == null) {
            System.out.println("WARNING: index.html, style.css and app.js must all be in src/main/resources/web. "
                    + "Add them, click the Maven reload icon, then run again.");
        }
        printLinks(port);
    }


    private void printLinks(int port) {
        String base = "http://localhost:" + port;

        System.out.println("Web server running. Click a link to open the website:");
        System.out.println("  Website  ->  " + base + "/");
        System.out.println();
        System.out.println("Or click the stop where you are waiting:");

        for (Route route : routes) {
            System.out.println();
            System.out.println(route.getName());
            for (Stop stop : route.getStops()) {

                String stopInLink = URLEncoder.encode(stop.getName(), StandardCharsets.UTF_8).replace("+", "%20");
                System.out.println("  " + stop.getName() + "  ->  " + base + "/?stop=" + stopInLink);
            }
        }
        System.out.println();
        System.out.println("(For developers: the raw data is at " + base + "/api/stops and " + base + "/api/arrivals?stop=Mundel)");
    }


    private void sendHome(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        String file;
        String contentType;
        if (path.equals("/") || path.equals("/index.html")) {
            file = "index.html";
            contentType = "text/html; charset=utf-8";
        } else if (path.equals("/style.css")) {
            file = "style.css";
            contentType = "text/css; charset=utf-8";
        } else if (path.equals("/app.js")) {
            file = "app.js";
            contentType = "text/javascript; charset=utf-8";
        } else {
            send(exchange, 404, "{\"error\":\"Not found\"}");
            return;
        }

        try (InputStream content = WebServer.class.getResourceAsStream("/web/" + file)) {
            if (content == null) {
                send(exchange, 404, "{\"error\":\"" + file + " not found in src/main/resources/web\"}");
                return;
            }
            sendBytes(exchange, 200, contentType, content.readAllBytes());
        }
    }

    private void sendRoutes(HttpExchange exchange) throws IOException {
        String json = "[";
        for (int i = 0; i < routes.size(); i++) {
            if (i > 0) {
                json = json + ",";
            }
            json = json + routes.get(i).toJson();
        }
        json = json + "]";
        send(exchange, 200, json);
    }

    /** Every stop once, with the routes that stop there. The passenger picks their stop from this list. */
    private void sendStops(HttpExchange exchange) throws IOException {
        Map<String, List<String>> routesAtStop = new LinkedHashMap<>();
        for (Route route : routes) {
            for (Stop stop : route.getStops()) {
                List<String> routeNames = routesAtStop.get(stop.getName());
                if (routeNames == null) {
                    routeNames = new ArrayList<>();
                    routesAtStop.put(stop.getName(), routeNames);
                }
                routeNames.add(route.getName());
            }
        }

        String json = "[";
        boolean firstStop = true;
        for (String stopName : routesAtStop.keySet()) {
            if (!firstStop) {
                json = json + ",";
            }
            firstStop = false;

            json = json + "{\"name\":\"" + stopName + "\",\"routes\":[";
            List<String> routeNames = routesAtStop.get(stopName);
            for (int i = 0; i < routeNames.size(); i++) {
                if (i > 0) {
                    json = json + ",";
                }
                json = json + "\"" + routeNames.get(i) + "\"";
            }
            json = json + "]}";
        }
        json = json + "]";
        send(exchange, 200, json);
    }

    /** The buses coming to the stop the passenger chose. Only buses of routes that stop there appear. */
    private void sendArrivals(HttpExchange exchange) throws IOException {
        // The address looks like /api/arrivals?stop=Mundel . Pick out the text after "stop=".
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("stop=")) {
            send(exchange, 400, "{\"error\":\"Use /api/arrivals?stop=Mundel\"}");
            return;
        }
        String stopName = query.substring("stop=".length()).trim();

        if (!stopExists(stopName)) {
            send(exchange, 404, "{\"error\":\"Unknown stop. See /api/stops for the list\"}");
            return;
        }

        // Ask every bus: "are you coming to this stop?"
        List<Arrival> arrivals = new ArrayList<>();
        for (Bus bus : buses) {
            Arrival arrival = bus.arrivalAt(stopName);
            if (arrival != null) {
                arrivals.add(arrival);
            }
        }

        // Nearest bus first
        arrivals.sort((a, b) -> Double.compare(a.getDistanceKm(), b.getDistanceKm()));

        String json = "[";
        for (int i = 0; i < arrivals.size(); i++) {
            if (i > 0) {
                json = json + ",";
            }
            json = json + arrivals.get(i).toJson();
        }
        json = json + "]";
        send(exchange, 200, json);
    }

    private boolean stopExists(String stopName) {
        for (Route route : routes) {
            if (route.indexOfStop(stopName) >= 0) {
                return true;
            }
        }
        return false;
    }

    private void sendBuses(HttpExchange exchange) throws IOException {
        // The address looks like /api/buses?route_id=1 . Pick out the number after "route_id=".
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("route_id=")) {
            send(exchange, 400, "{\"error\":\"Use /api/buses?route_id=1\"}");
            return;
        }

        int routeId;
        try {
            routeId = Integer.parseInt(query.substring("route_id=".length()));
        } catch (NumberFormatException e) {
            send(exchange, 400, "{\"error\":\"route_id must be a number\"}");
            return;
        }

        // Collect the buses that belong to this route and have already left
        String json = "[";
        boolean first = true;
        for (Bus bus : buses) {
            if (bus.getRouteId() == routeId && bus.hasDeparted()) {
                if (!first) {
                    json = json + ",";
                }
                json = json + bus.toJson();
                first = false;
            }
        }
        json = json + "]";
        send(exchange, 200, json);
    }

    /** Sends JSON text back to the browser. */
    private void send(HttpExchange exchange, int status, String json) throws IOException {
        sendBytes(exchange, status, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }

    /** Sends any answer (the web page or JSON) back to the browser. */
    private void sendBytes(HttpExchange exchange, int status, String contentType, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        // Lets a web page on another address read the answers too.
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);

        OutputStream out = exchange.getResponseBody();
        out.write(bytes);
        out.close();
    }
}
