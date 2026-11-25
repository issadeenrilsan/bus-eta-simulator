# Bus ETA Simulator

Buses drive along routes. A small Java web server tells a web page each bus's name, next stop,
distance and ETA. Java + MySQL (JDBC) + Maven. No frameworks.

## 1. Open it in IntelliJ
1. Unzip the file.
2. IntelliJ: **File > Open**, choose the `bus-eta` folder (the one with `pom.xml`), **Open as Project**, **Trust Project**.
3. Wait for the progress bar (bottom right). Maven is downloading the MySQL driver.
4. If IntelliJ asks for a Project SDK, choose Java 11 or newer.

## 2. Set up the database (MySQL Workbench)
One file builds everything: `sql/bus_tracker_System.sql`.
1. In Workbench choose **File > Open SQL Script** and pick that file.
2. Click the lightning bolt to run the whole file.
3. The last lines show a check table: 3 routes, with 15 / 7 / 9 stops and 6 buses each.

It creates a database called **bus_tracker_System** with 3 tables (`routes`, `Stops`, `Buses`), 3 routes
and 18 buses. Your old `bus_tracker` database is not touched.
Running the file again **deletes and rebuilds** `bus_tracker_System`. That is handy for starting over,
but do not run it again after you have added data of your own that you want to keep.

## 3. Run it
Open `src/main/java/com/buseta/Main.java`, click the green play button, **Run 'Main.main()'**.
The console should show:

    Loaded 3 route(s) and 18 bus(es).
    Web server running. Click a link to open the website:
      Website  ->  http://localhost:8080/
    ... (then a link for every stop)

**Click the Website link** (hold Ctrl and click if a plain click does nothing). The website opens in your browser:
1. Type any part of your stop's name in the search box, for example `kuru`, `bus` or `mundel` (capital letters do not matter),
   and pick your stop from the list. Use the arrow keys and Enter, or click.
2. Picking a stop shows its buses at once. (**Find my bus** does the same for whatever you typed.) You see a card for every bus that is still coming to that stop, nearest first: the bus name,
   its route, where it is going, and the minutes until it arrives. The list refreshes by itself every 3 seconds.
3. The moon/sun button in the top bar switches between light and dark.

The links for the stops open the page with that stop already chosen, for example `http://localhost:8080/?stop=Mundel`.

**What a passenger sees:** only the buses that are still coming to the stop they chose.
At Kurunegala or Nittambuwa that is the Colombo - Anuradhapura buses. At Mundel it is the Chilaw - Puttalam buses.
Puttalam Bus Stand is served by two routes, so buses of both routes appear (each card shows its route).

**Buses leave one minute apart.** The first bus of each route leaves at once, the next one a minute later,
and so on. A bus that has not left yet is not shown, so you see one bus at first and more as the minutes pass.

Press the red square in IntelliJ to stop the program.

### The raw data behind the page (for developers)
The page gets its information from these addresses. Open them in a browser to see the plain data:
- `/api/stops` every stop and which routes stop there
- `/api/arrivals?stop=Mundel` the buses coming to one stop, nearest first
- `/api/routes` the routes
- `/api/buses?route_id=1` all the buses of one route

## Simulation settings (in the code)
| Setting | File | Meaning |
|---|---|---|
| `SECONDS_BETWEEN_BUSES = 60` | `Database.java` | how long after one bus the next one leaves |
| `SPEED_UP = 10` | `Bus.java` | the demo runs 10 times faster than real life, so you do not wait hours. The ETA uses the same speed, so it is true to what you watch. Set it to 1 for real time. |

## How the files fit together (read them in this order)
| File | Its one job | Proposal module |
|---|---|---|
| `Main.java` | Starts everything | |
| `Stop.java`, `Route.java` | Hold a stop / a route (a route can find a stop by name) | Route & Stop Management |
| `Database.java` | **All** the JDBC: connect, read, save | Database Update |
| `Bus.java` | One bus: where it is, how it moves, how far it is from any stop | Position Simulation |
| `Arrival.java` | One bus coming to the passenger's stop | ETA Calculation |
| `Geo.java` | Haversine distance | ETA Calculation |
| `Simulator.java` | Every 2 seconds: move each bus, save its position | Position Simulation |
| `WebServer.java` | Sends the web page and the data addresses the page asks | Web Interface (backend) |
| `src/main/resources/web/index.html` | **HTML: the website's content** (headline, search box, sections) | Web Frontend |
| `src/main/resources/web/style.css` | **CSS: the website's look** (colours, layout, light/dark theme, small screens) | Web Frontend |
| `src/main/resources/web/app.js` | **JavaScript: the website's behaviour** (search box, asks the server for buses, draws the cards, theme button) | Web Frontend |

## Settings
Only needed when you are NOT using the defaults. Set them in IntelliJ under
**Run > Edit Configurations > Environment variables**.

| Name | Default |
|---|---|
| DB_URL | jdbc:mysql://localhost:3306/bus_tracker_System |
| DB_USER | root |
| DB_PASSWORD | (empty) |
| PORT | 8080 |

## Common problems
| Message | Fix |
|---|---|
| Could not read from the database ... Connection refused | MySQL is not running. Start it. |
| Access denied for user 'root' | Set DB_PASSWORD (see Settings). |
| Unknown database 'bus_tracker_System' | Run `sql/bus_tracker_System.sql` in Workbench. |
| Address already in use | Set PORT=8081 and use 8081 in the browser. |
| No suitable driver found | Click the Maven reload icon in IntelliJ. |
| Table ... doesn't exist | Names must be exactly `routes`, `Stops`, `Buses`. |
| The page says `index.html not found`, or it looks plain with no colours | `index.html`, `style.css` and `app.js` must all be in `src/main/resources/web`. Click the Maven reload icon, then **Build > Rebuild Project**, and run again. |
| `/api/arrivals` shows `[]` | No bus has left yet (wait a minute), or no bus is coming to that stop. |
| `Unknown stop` | The stop name must match `/api/stops` (capital letters do not matter). |

## Cloud (later)
Database on Aiven (free MySQL), server on Render (free, Docker, uses the included `Dockerfile`).
In the cloud you cannot create your own database, so run `bus_tracker_System.sql` there WITHOUT its first
three statements (DROP DATABASE, CREATE DATABASE, USE): use the database your host gives you instead.
In Render set DB_URL (`jdbc:mysql://HOST:PORT/DATABASE?sslMode=REQUIRED`), DB_USER and DB_PASSWORD.
Render sets PORT itself. Render's free service sleeps after 15 idle minutes, and buses restart from
their first stops when it wakes.
