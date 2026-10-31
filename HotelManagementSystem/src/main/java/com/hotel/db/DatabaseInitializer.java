package com.hotel.db;

import com.hotel.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/** Creates the database, tables, default admin and sample rooms on first run. */
public class DatabaseInitializer {

    public static void init() throws Exception {
        try (Connection con = DBConnection.getServerConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DBConnection.DB_NAME);
        }
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS admin (admin_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "username VARCHAR(50) NOT NULL UNIQUE, password VARCHAR(100) NOT NULL,"
                    + "full_name VARCHAR(100) NOT NULL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS rooms (room_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "room_number VARCHAR(10) NOT NULL UNIQUE, room_type VARCHAR(20) NOT NULL,"
                    + "price DECIMAL(10,2) NOT NULL, capacity INT NOT NULL,"
                    + "status VARCHAR(20) NOT NULL DEFAULT 'Available')");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS customers (customer_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "name VARCHAR(100) NOT NULL, phone VARCHAR(20) NOT NULL, email VARCHAR(100),"
                    + "address VARCHAR(255), id_proof VARCHAR(100))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS bookings (booking_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "booking_code VARCHAR(20) UNIQUE, customer_id INT NOT NULL, room_id INT NOT NULL,"
                    + "check_in DATE NOT NULL, check_out DATE NOT NULL, total_amount DECIMAL(10,2) NOT NULL,"
                    + "final_bill DECIMAL(10,2) NULL, status VARCHAR(20) NOT NULL DEFAULT 'Booked',"
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (customer_id) REFERENCES customers(customer_id),"
                    + "FOREIGN KEY (room_id) REFERENCES rooms(room_id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS payments (payment_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "receipt_no VARCHAR(20) UNIQUE, booking_id INT NOT NULL, amount DECIMAL(10,2) NOT NULL,"
                    + "method VARCHAR(10) NOT NULL, payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (booking_id) REFERENCES bookings(booking_id))");

            // Default admin
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM admin")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO admin(username,password,full_name) VALUES(?,?,?)")) {
                        ps.setString(1, "admin");
                        ps.setString(2, Util.hash("admin123"));
                        ps.setString(3, "Hotel Administrator");
                        ps.executeUpdate();
                    }
                }
            }
            // Sample rooms
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM rooms")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    String[][] rooms = {{"101", "Single", "1500", "1"}, {"102", "Single", "1500", "1"},
                            {"201", "Double", "2500", "2"}, {"202", "Double", "2500", "2"},
                            {"301", "Deluxe", "4000", "3"}, {"401", "Suite", "7000", "4"}};
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO rooms(room_number,room_type,price,capacity,status) VALUES(?,?,?,?,'Available')")) {
                        for (String[] r : rooms) {
                            ps.setString(1, r[0]);
                            ps.setString(2, r[1]);
                            ps.setDouble(3, Double.parseDouble(r[2]));
                            ps.setInt(4, Integer.parseInt(r[3]));
                            ps.executeUpdate();
                        }
                    }
                }
            }
        }
    }
}
