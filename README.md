# BusNow.lk 🚌

A live bus tracking web app for Sri Lankan bus routes. Passengers search for the stop they are waiting at and see only the buses still coming to that stop, nearest first, with a live ETA that refreshes every few seconds.

Built with **Java + MySQL (JDBC) + Maven**, no frameworks: the web server, the API and the simulation are written from scratch.



---

## Features

- **Stop search:** type part of a stop name (e.g. `kuru`, `mundel`) and pick from the list
- **Live arrivals:** a card for every bus still coming to your stop, showing route, destination and minutes until arrival, refreshed every 3 seconds
- **4 real routes :
  - Kalpitiya → Puttalam
  - Chilaw → Puttalam
  - Colombo → Anuradhapura (via Kurunegala and Dambulla)
  -  Baticola → Trincomalee
- **Shared stops:** Puttalam Bus Stand is served by two routes, so buses from both appear
- **Realistic departures:** buses on each route leave one minute apart
- **Light / dark theme** and a layout that works on small screens
- **Direct stop links**, e.g. `http://localhost:8080/?stop=Mundel`

## How it works

- `Simulator` moves every bus along its route every 2 seconds and saves its position to MySQL.
- Distance from a bus to a stop is calculated with the **Haversine formula** (great-circle distance between GPS coordinates), and the ETA follows from that distance and the bus speed.
- A small web server built on Java's built-in `HttpServer` serves the website and a JSON API.
- All database access goes through one class (`Database.java`) using JDBC Prepared Statements.
- Database credentials are read from environment variables, so no passwords live in the code.

## Tech stack

| Part | Technology |
|---|---|
| Backend | Java 11+, built-in `HttpServer` |
| Frontend | HTML, CSS, JavaScript |
| Database | MySQL, JDBC |
| Build | Maven |
| Container | Docker (Dockerfile included) |

## API

| Endpoint | Returns |
|---|---|
| `/api/stops` | Every stop and which routes stop there |
| `/api/arrivals?stop=Mundel` | Buses coming to one stop, nearest first |
| `/api/routes` | All routes |
| `/api/buses?route_id=1` | All buses on one route |

## Project structure

| File | Its one job |
|---|---|
| `Main.java` | Starts everything |
| `Stop.java`, `Route.java` | Hold a stop / a route (a route can find a stop by name) |
| `Database.java` | All the JDBC: connect, read, save |
| `Bus.java` | One bus: where it is, how it moves, how far it is from any stop |
| `Arrival.java` | One bus coming to the passenger's stop |
| `Geo.java` | Haversine distance |
| `Simulator.java` | Every 2 seconds: move each bus, save its position |
| `WebServer.java` | Serves the web page and the API |
| `resources/web/index.html` | Website content |
| `resources/web/style.css` | Website look (colours, layout, light/dark theme) |
| `resources/web/app.js` | Website behaviour (search, fetches buses, draws cards, theme button) |
| `sql/bus_tracker_System.sql` | Builds the database: tables, routes, stops and buses |

---

## Run it locally

### 1. Open in IntelliJ
1. **File > Open**, choose the `bus-eta` folder (the one with `pom.xml`), **Open as Project**, **Trust Project**.
2. Wait for Maven to finish downloading the MySQL driver (progress bar, bottom right).
3. If asked for a Project SDK, choose Java 11 or newer.

### 2. Set up the database
1. In MySQL Workbench: **File > Open SQL Script** and choose `sql/bus_tracker_System.sql`.
2. Click the lightning bolt to run the whole file.
3. The last lines show a check table: 3 routes, with 15 / 7 / 9 stops and 6 buses each.

This creates a database called `bus_tracker_System` with 3 tables (`routes`, `Stops`, `Buses`).
⚠️ Running the file again **deletes and rebuilds** the database.

### 3. Run
Open `src/main/java/com/buseta/Main.java` and click **Run 'Main.main()'**. The console shows:

    Loaded 3 route(s) and 18 bus(es).
    Web server running. Click a link to open the website:
      Website  ->  http://localhost:8080/

Open the link (Ctrl + click), then search for your stop.

### Settings
Only needed if you are not using the defaults. Set them in IntelliJ under **Run > Edit Configurations > Environment variables**.

| Name | Default |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/bus_tracker_System` |
| `DB_USER` | `root` |
| `DB_PASSWORD` | (empty) |
| `PORT` | `8080` |

Simulation settings in the code:

| Setting | File | Meaning |
|---|---|---|
| `SECONDS_BETWEEN_BUSES = 60` | `Database.java` | Time between one bus leaving and the next |
| `SPEED_UP = 10` | `Bus.java` | Demo runs 10× faster than real life. The ETA uses the same speed, so it stays accurate. Set to 1 for real time. |

### Common problems

| Message | Fix |
|---|---|
| Connection refused | MySQL is not running. Start it. |
| Access denied for user 'root' | Set `DB_PASSWORD` (see Settings). |
| Unknown database 'bus_tracker_System' | Run `sql/bus_tracker_System.sql`. |
| Address already in use | Set `PORT=8081` and use 8081 in the browser. |
| No suitable driver found | Click the Maven reload icon in IntelliJ. |
| Table ... doesn't exist | Table names must be exactly `routes`, `Stops`, `Buses`. |
| `index.html not found`, or the page has no styling | `index.html`, `style.css` and `app.js` must be in `src/main/resources/web`. Reload Maven, **Build > Rebuild Project**, run again. |
| `/api/arrivals` shows `[]` | No bus has left yet (wait a minute), or no bus serves that stop. |
| `Unknown stop` | The stop name must match one from `/api/stops`. |

---

## Deploying to the cloud (not deployed yet)

The project is set up so it can be deployed with the database on **Aiven** (free MySQL) and the server on **Render** (free, uses the included `Dockerfile`).

- Run `bus_tracker_System.sql` on Aiven **without** its first three statements (`DROP DATABASE`, `CREATE DATABASE`, `USE`), and use the database Aiven gives you.
- In Render, set `DB_URL` (`jdbc:mysql://HOST:PORT/DATABASE?sslMode=REQUIRED`), `DB_USER` and `DB_PASSWORD`. Render sets `PORT` itself.
- Render's free service sleeps after 15 idle minutes, and buses restart from their first stops when it wakes.

## Future plans

- Add more routes, aiming to cover the whole island of Sri Lanka
- Connect to real GPS data from buses

## Author

**Mohammed Rilsan** · [GitHub](https://github.com/issadeenrilsan)
