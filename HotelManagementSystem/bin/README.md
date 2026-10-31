# Hotel Room Management System (Java Swing + MySQL + JDBC + Maven)

## Run in 5 steps
1. Install JDK 17+ and MySQL Server 8 (keep it running).
2. IntelliJ: File > Open > select this folder (HotelManagementSystem) > "Trust Project".
   If asked, choose "Load Maven Project" (or right-click pom.xml > Maven > Reload project).
3. Open src/main/java/com/hotel/db/DBConnection.java and set DB_PASSWORD to YOUR MySQL root password.
4. Open src/main/java/com/hotel/Main.java, click the green triangle next to `main` > Run 'Main.main()'.
5. Login:  admin / admin123

The database `hotel_management`, all tables, the admin user and 6 sample rooms
are created automatically on first run (schema.sql in resources is for reference only).

## Dates
Booking dates are typed as yyyy-MM-dd (e.g. 2026-11-05). Check-in cannot be in the past.

## Room status flow
Available --book--> Booked --check-in--> Occupied --check-out--> Available
(Cancelling a 'Booked' booking also frees the room.)

## Build a runnable jar
mvn clean package   ->  java -jar target/HotelManagementSystem-1.0.jar
