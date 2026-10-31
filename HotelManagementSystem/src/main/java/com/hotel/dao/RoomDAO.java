package com.hotel.dao;

import com.hotel.db.DBConnection;
import com.hotel.model.Room;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** All database operations for rooms. */
public class RoomDAO extends BaseDAO {
    private static final String[] HEADERS = {"ID", "Room No", "Type", "Price", "Capacity", "Status"};

    public void addRoom(Room r) throws Exception {
        String sql = "INSERT INTO rooms(room_number,room_type,price,capacity,status) VALUES(?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getRoomNumber());
            ps.setString(2, r.getRoomType());
            ps.setDouble(3, r.getPrice());
            ps.setInt(4, r.getCapacity());
            ps.setString(5, r.getStatus());
            ps.executeUpdate();
        }
    }

    public void updateRoom(Room r) throws Exception {
        String sql = "UPDATE rooms SET room_number=?, room_type=?, price=?, capacity=?, status=? WHERE room_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getRoomNumber());
            ps.setString(2, r.getRoomType());
            ps.setDouble(3, r.getPrice());
            ps.setInt(4, r.getCapacity());
            ps.setString(5, r.getStatus());
            ps.setInt(6, r.getRoomId());
            ps.executeUpdate();
        }
    }

    public void deleteRoom(int roomId) throws Exception {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM rooms WHERE room_id=?")) {
            ps.setInt(1, roomId);
            ps.executeUpdate();
        }
    }

    public DefaultTableModel getAllRooms() throws Exception {
        return table("SELECT room_id, room_number, room_type, price, capacity, status FROM rooms ORDER BY room_number", HEADERS);
    }

    public DefaultTableModel getRoomsByStatus(String status) throws Exception {
        return table("SELECT room_id, room_number, room_type, price, capacity, status FROM rooms WHERE status=? ORDER BY room_number",
                HEADERS, status);
    }

    /** Rooms that can currently be booked. */
    public List<Room> getAvailableRooms() throws Exception {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT room_id, room_number, room_type, price, capacity, status FROM rooms WHERE status='Available' ORDER BY room_number";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Room(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getInt(5), rs.getString(6)));
            }
        }
        return list;
    }

    /** Rooms that are Booked or Occupied. */
    public DefaultTableModel getUnavailableRooms() throws Exception {
        return table("SELECT room_id, room_number, room_type, price, capacity, status FROM rooms WHERE status<>'Available' ORDER BY room_number",
                HEADERS);
    }
}
