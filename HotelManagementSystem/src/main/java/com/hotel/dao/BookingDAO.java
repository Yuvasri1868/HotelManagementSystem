package com.hotel.dao;

import com.hotel.db.DBConnection;
import com.hotel.model.Booking;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Booking, check-in and check-out operations. */
public class BookingDAO extends BaseDAO {

    private static final String SELECT_ALL =
            "SELECT b.booking_id, b.booking_code, c.name, r.room_number, r.room_type, b.check_in, b.check_out, "
                    + "b.total_amount, b.final_bill, b.status FROM bookings b "
                    + "JOIN customers c ON b.customer_id=c.customer_id JOIN rooms r ON b.room_id=r.room_id ";
    private static final String[] HEADERS = {"ID", "Booking Code", "Customer", "Room", "Type", "Check-In",
            "Check-Out", "Total", "Final Bill", "Status"};

    /** Number of nights between two dates (minimum 1). */
    public static long nights(LocalDate in, LocalDate out) {
        return Math.max(1, ChronoUnit.DAYS.between(in, out));
    }

    /** True if no active booking of this room overlaps the requested dates (prevents double booking). */
    public boolean isRoomFree(int roomId, LocalDate in, LocalDate out) throws Exception {
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_id=? AND status IN ('Booked','Checked-In') "
                + "AND check_in < ? AND check_out > ?";
        return count(sql, roomId, Date.valueOf(out), Date.valueOf(in)) == 0;
    }

    /** Creates a booking, generates the booking code and marks the room as Booked. Returns the booking code. */
    public String createBooking(Booking b, double pricePerNight) throws Exception {
        if (!isRoomFree(b.getRoomId(), b.getCheckIn(), b.getCheckOut()))
            throw new Exception("This room is already booked for the selected dates.");
        double total = nights(b.getCheckIn(), b.getCheckOut()) * pricePerNight;
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO bookings(customer_id,room_id,check_in,check_out,total_amount,status) VALUES(?,?,?,?,?,'Booked')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, b.getCustomerId());
                    ps.setInt(2, b.getRoomId());
                    ps.setDate(3, Date.valueOf(b.getCheckIn()));
                    ps.setDate(4, Date.valueOf(b.getCheckOut()));
                    ps.setDouble(5, total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                String code = "BK" + (1000 + id);   // booking ID e.g. BK1001
                try (PreparedStatement ps = con.prepareStatement("UPDATE bookings SET booking_code=? WHERE booking_id=?")) {
                    ps.setString(1, code);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE rooms SET status='Booked' WHERE room_id=?")) {
                    ps.setInt(1, b.getRoomId());
                    ps.executeUpdate();
                }
                con.commit();
                return code;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Check-in: booking must be in 'Booked' status. Room becomes Occupied. */
    public void checkIn(int bookingId) throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int roomId = -1;
                try (PreparedStatement ps = con.prepareStatement("SELECT room_id, status FROM bookings WHERE booking_id=?")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new Exception("Booking not found.");
                        if (!"Booked".equals(rs.getString("status")))
                            throw new Exception("Only bookings with status 'Booked' can be checked in.");
                        roomId = rs.getInt("room_id");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE bookings SET status='Checked-In' WHERE booking_id=?")) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE rooms SET status='Occupied' WHERE room_id=?")) {
                    ps.setInt(1, roomId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Check-out: calculates the final bill (stayed nights x price; at least the booked nights),
     * frees the room and returns the final bill.
     */
    public double checkOut(int bookingId) throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int roomId;
                LocalDate in, out;
                double price;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT b.room_id, b.check_in, b.check_out, b.status, r.price FROM bookings b "
                                + "JOIN rooms r ON b.room_id=r.room_id WHERE b.booking_id=?")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new Exception("Booking not found.");
                        if (!"Checked-In".equals(rs.getString("status")))
                            throw new Exception("Only checked-in guests can be checked out.");
                        roomId = rs.getInt("room_id");
                        in = rs.getDate("check_in").toLocalDate();
                        out = rs.getDate("check_out").toLocalDate();
                        price = rs.getDouble("price");
                    }
                }
                LocalDate today = LocalDate.now();
                long billed = Math.max(nights(in, out), nights(in, today));   // extra days are charged
                double finalBill = billed * price;
                LocalDate newOut = today.isAfter(out) ? today : out;
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE bookings SET status='Checked-Out', final_bill=?, check_out=? WHERE booking_id=?")) {
                    ps.setDouble(1, finalBill);
                    ps.setDate(2, Date.valueOf(newOut));
                    ps.setInt(3, bookingId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE rooms SET status='Available' WHERE room_id=?")) {
                    ps.setInt(1, roomId);
                    ps.executeUpdate();
                }
                con.commit();
                return finalBill;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Cancels a booking that has not been checked in yet and frees the room. */
    public void cancel(int bookingId) throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int roomId;
                try (PreparedStatement ps = con.prepareStatement("SELECT room_id, status FROM bookings WHERE booking_id=?")) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new Exception("Booking not found.");
                        if (!"Booked".equals(rs.getString("status")))
                            throw new Exception("Only bookings with status 'Booked' can be cancelled.");
                        roomId = rs.getInt("room_id");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE bookings SET status='Cancelled' WHERE booking_id=?")) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE rooms SET status='Available' WHERE room_id=?")) {
                    ps.setInt(1, roomId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    public DefaultTableModel getAllBookings() throws Exception {
        return table(SELECT_ALL + "ORDER BY b.booking_id DESC", HEADERS);
    }

    public DefaultTableModel getBookingsByStatus(String status) throws Exception {
        return table(SELECT_ALL + "WHERE b.status=? ORDER BY b.booking_id DESC", HEADERS, status);
    }

    /** Bookings that still need action (Booked or Checked-In) - used by Check-In/Out screen & payments. */
    public DefaultTableModel getActiveBookings() throws Exception {
        return table(SELECT_ALL + "WHERE b.status IN ('Booked','Checked-In') ORDER BY b.booking_id DESC", HEADERS);
    }

    /** Bill amount: final bill if checked out, otherwise the booked total. */
    public double getBillAmount(int bookingId) throws Exception {
        String sql = "SELECT COALESCE(final_bill, total_amount) FROM bookings WHERE booking_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }
}
