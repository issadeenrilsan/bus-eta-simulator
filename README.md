# BusNow.lk 🚌

A live bus tracking web app for Sri Lankan bus routes. Passengers choose the stop they are waiting at and see only the buses heading to that stop, with a live ETA for each one.



![BusNow.lk screenshot](screenshots/home.png)


---

## Features

- **Stop-based view:** pick your stop (e.g. Puttalam, Kurunegala) and see only the buses coming to you
- **Live ETAs:** arrival times update automatically as buses move along the route
- **Multiple routes and buses:** several buses run on each route, leaving about one minute apart
- **Real routes:**
  - Kalpitiya → Puttalam
  - Chilaw → Puttalam
  - Colombo → Anuradhapura (via Kurunegala and Dambulla)

## How it works

- Bus positions are simulated along real GPS coordinates for each route.
- Distance between a bus and a stop is calculated with the **Haversine formula** (great-circle distance between two latitude/longitude points), and the ETA is worked out from that distance and the bus speed.
- The Java backend serves the web pages and provides the live bus data to the frontend.
- Routes, stops and buses are stored in a **MySQL** database, accessed through **JDBC** using Prepared Statements (protecting against SQL injection).

## Tech stack

| Part | Technology |
|---|---|
| Backend | Java, built-in `HttpServer` (no framework) |
| Frontend | HTML, CSS, JavaScript |
| Database | MySQL, JDBC |
| Build | Maven |
| Deployment | Docker |

## Project structure

```
bus-eta/
├── sql/
│   └── bus_tracker_System.sql     # Database schema and route data
├── src/main/
│   ├── java/com/buseta/           # Backend: Main, Simulator, Route, Stop, Bus, Arrival, Geo, Database
│   └── resources/web/             # Frontend: index.html, style.css, JavaScript
├── Dockerfile
└── pom.xml
```

## Run it locally

**Requirements:** Java 17+, Maven, MySQL

1. **Set up the database**
   ```bash
   mysql -u root < sql/bus_tracker_System.sql
   ```
   Default connection: MySQL on `localhost:3306`, user `root`, no password. Change these in `Database.java` if your setup is different.

2. **Build and run**
   ```bash
   mvn clean package
   java -jar target/<your-jar-name>.jar
   ```
   Or simply open the project in IntelliJ IDEA and run `Main.java`.

3. **Open in your browser:** `http://localhost:8080`

## Future plans

- Add more routes, with the long-term goal of covering the whole island of Sri Lanka
- Connect to real GPS data from buses

## Author

**Mohammed Rilsan** · [GitHub](https://github.com/issadeenrilsan)
