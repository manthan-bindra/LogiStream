# LogiStream — Java Logistics Manager

LogiStream is a console-based last-mile courier operations manager. Its start screen offers staff login, customer access, and exit. Customers can register/login, track their parcels, and register new deliveries. Staff can view customer and delivery records, manage drivers and vehicles, start shifts, dispatch parcels, simulate deliveries, and view reports.

> **Evaluation scope:** This build focuses on the topics in Lectures 1–36. Customer accounts and customer portal parcels persist in MySQL. Staff operations demo data (drivers, vehicles, shifts, routes, and dispatch simulation) remains in memory for each run.

## 1. Run the application

Requirements: JDK 17, MySQL Server 8.0 or later, Maven, and the VS Code Java Extension Pack. Maven downloads MySQL Connector/J on the first run (internet access is needed).

- In VS Code, open this project folder and click **Run** for `LogiStreamApp.java` (or select the **Run LogiStream** launch profile).
- Terminal alternative: `mvn compile exec:java`.

The main console application uses Maven to include MySQL Connector/J. Before running the app, create the `logistream` database in MySQL and configure credentials as described below.


## 2. Login and main menu

The first screen offers:

1. Login as staff member
2. Continue as customer
0. Exit

Customer access then offers new customer registration or login. After signing in, customers can view their parcel statuses or register another parcel. Staff sign in using the seeded staff account, then see a dashboard, Customer & Delivery Records, Logistics Operations, and a Coming Soon list of advanced features. Reports are grouped under Logistics Operations.

On the first database initialization, the app creates the staff account configured by `LOGISTREAM_STAFF_USERNAME` and `LOGISTREAM_STAFF_PASSWORD` (defaults: `staff` / `staff123`). Staff passwords can be changed in `.env` before first initialization. Customer and staff passwords are stored as salted PBKDF2 hashes, not plaintext.

Load demo logistics data from the staff portal to populate the in-memory driver/shift/dispatch screens. Customer account and portal parcel records are persisted in MySQL.

Suggested demonstration: log in as staff, load demo data, open **Logistics Operations → Shifts, Dispatch & Delivery**, start shifts for the sample drivers, dispatch pending parcels, view routes, and run the delivery simulation. Then open **Reports & Analytics** and try the collections showcase. The simulation starts driver threads and a notification consumer thread.

## 3. Package structure

- `com.logistream` — application entry point.
- `app` — application object graph and demonstration data setup.
- `model` — customers, parcels, drivers, vehicles, routes, shifts, enums and parcel factory.
- `repository` — generic repository interface and in-memory repository.
- `service` — business rules, dispatching, reporting and comparisons.
- `ui` — menu interface, abstract menu and console menus.
- `util` — shared console/date/input helpers and zone-distance matrix.
- `exception` — custom application exceptions.
- `concurrent` — delivery threads, counters and producer-consumer event buffer.
- `database` — MySQL connection, account security, portal DAO, and customer parcel persistence.

The project has **40 top-level Java classes**. Enums and interfaces are additional source types.

## 4. Syllabus mapping (Lectures 1–36)

| Lectures | Syllabus topic | Example implementation |
|---|---|---|
| 1–5 | OOP, classes, objects, methods, constructors, `this`/`static`, inheritance, polymorphism, abstraction, interfaces | `Entity`, `Parcel`, `StandardParcel`, `ExpressParcel`, `FragileParcel`, `Vehicle`, `Bike`, `Van`, `Menu`, `Repository`; overloaded and overridden methods |
| 6–9 | Multidimensional arrays, String, String Pool, comparison/search/manipulation/formatting, StringBuilder/StringBuffer, wrappers | `ZoneMap` distance matrix; string validation/comparison; `StringBuilder` reports; `StringBuffer`, String Pool `intern()`, and numeric wrapper parsing |
| 10–12 | Checked/unchecked exceptions, try/catch/finally, multi-catch, propagation, throw/throws, custom exceptions | `LogiStreamException`; guarded menu/input operations |
| 13–15 | Integrated coding, debugging, input validation, console coding practices | `InputHelper`, `LogiStreamUtil`, menu-driven workflows and demo scenarios |
| 16–19 | Collections Framework, ArrayList, LinkedList, HashSet, LinkedHashSet, TreeSet | `ArrayList`, `LinkedList`, `HashSet`, `LinkedHashSet`, `TreeSet`; routes maintain ordered stops |
| 20–22 | HashMap, LinkedHashMap, TreeMap, Queue, Deque, PriorityQueue, Iterator/ListIterator | Dispatch uses `PriorityQueue`, `Queue`, `HashMap`, `LinkedHashMap`; reports use `TreeMap`; routes use `Iterator`; showcase uses `Deque` and `ListIterator` |
| 23–25 | Comparable, Comparator, sorting, Collections utility methods | `Parcel` natural ordering and a delivery-count comparator in `ReportService` |
| 26–27 | Multiprogramming, multithreading, concurrency, Thread, Runnable, start/run/sleep/join/interrupt | `DriverWorker`, `NotificationWorker`, `DeliverySimulator`, and `AtomicInteger` |
| 28–29 | Thread lifecycle, scheduling, and tracing | `DriverWorker` and `DeliverySimulator` show thread creation, sleep, start, and join; extended lifecycle demos are listed under Coming Soon |
| 30–31 | Shared data, race conditions, critical sections, synchronized methods/blocks, intrinsic locks, atomicity and visibility | Synchronized generic repository methods, `EventBuffer`, and `AtomicInteger` updates |
| 32–33 | wait(), notify(), notifyAll(), guarded blocks, producer-consumer | `EventBuffer` uses guarded `wait()`/`notifyAll()` for producer-consumer coordination |
| 34–36 | JDBC, relational/SQL concepts, JDBC architecture, drivers, URL, DriverManager, Connection, Statement, PreparedStatement, ResultSet, CRUD | `DatabaseConnection` and `PortalDao` |

## 5. JDBC classroom demonstration

Customer accounts and portal parcel records are stored in the MySQL `logistream` database and remain available after the program exits. Before running the app, start MySQL and create the database (for example, in MySQL Workbench):

```sql
CREATE DATABASE logistream;
```

The default connection is `jdbc:mysql://localhost:3306/logistream`, user `root`. The VS Code launch profile loads credentials from the local `.env` file. Copy `.env.example` to `.env` and set `LOGISTREAM_DB_PASSWORD` to your MySQL password. `.env` is ignored by Git so your password is not committed. The application cannot discover or reset your MySQL password.

The startup flow creates namespaced `logistream_*` tables so it does not collide with other tables such as an existing `customers` table. Customer registration demonstrates `INSERT` and a transaction; login demonstrates `SELECT`; parcel tracking demonstrates `SELECT`/`ResultSet`; staff status changes demonstrate `UPDATE`. Queries bind values with `PreparedStatement`, and database work uses try-with-resources.

The JDBC implementation is in `database/DatabaseConnection.java` and `database/PortalDao.java`. `PortalDao` keeps SQL and database access separate from the menus. Customer and staff passwords use salted PBKDF2 hashes. MySQL Connector/J is declared in `pom.xml`; Maven adds it to the run classpath.

For a live demonstration, start the program, choose customer access, register a customer with a username/password, register a parcel, and log out. Sign in as staff (`staff` / `staff123` by default), open the system overview, and see the customer and parcel rows alongside driver, shift, route, and delivery information. Staff can update a customer parcel's delivery status. Close and restart the app, sign in again, and confirm those MySQL records remain.

## 6. Coming Soon — Advanced Features

These higher-difficulty features are listed in the staff portal and planned for later project milestones. They are not implemented yet:

- Optimize multi-stop delivery routes using zone distances, parcel priority, and vehicle capacity.
- Persist drivers, vehicles, shifts, routes, and delivery events in MySQL.
- Add a parcel tracking timeline with timestamps and estimated delivery windows.
- Add transaction-based billing with payment and refund records.
- Import and export parcel/customer data as validated CSV, then schedule summary reports.
- Add staff roles and permissions, account recovery, and an audit trail for administrative changes.
- Reassign parcels between active routes with vehicle-capacity checks.
- Undo recent parcel status changes with a clear history of what was reverted.
- Restore the interactive thread-state and `wait()`/`notify()` classroom demonstration.

Undo, route reallocation, and the separate interactive thread demo were removed from the active menus and moved here for a later milestone. Repetitive CRUD repositories, portal submenus, and small helpers were consolidated to reach 40 top-level classes. The core domain model, business services, generic repository, customer portal, staff dashboard, dispatch workflow, and delivery simulation remain in the active project.
# LogiStream
