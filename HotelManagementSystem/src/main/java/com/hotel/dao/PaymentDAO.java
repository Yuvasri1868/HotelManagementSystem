package com.hotel.dao;

import com.hotel.db.DBConnection;
import com.hotel.model.Payment;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Payment recording, history and receipts. */
public class PaymentDAO extends BaseDAO {

    private static final String SELECT_ALL =
            "SELECT p.payment_id, p.receipt_no, b.booking_code, c.name, p.amount, p.method, p.payment_date "
                    + "FROM payments p JOIN bookings b ON p.booking_id=b.booking_id "
                    + "JOIN customers c ON b.customer_id=c.customer_id ";
    private static final String[] HEADERS = {"ID", "Receipt No", "Booking", "Customer", "Amount", "Method", "Date"};

    public double totalPaid(int bookingId) throws Exception {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM payments WHERE booking_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    /** Saves the payment and returns its receipt number. */
    public String addPayment(Payment p) throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO payments(booking_id,amount,method) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, p.getBookingId());
                    ps.setDouble(2, p.getAmount());
                    ps.setString(3, p.getMethod());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                String receipt = "RC" + (5000 + id);
                try (PreparedStatement ps = con.prepareStatement("UPDATE payments SET receipt_no=? WHERE payment_id=?")) {
                    ps.setString(1, receipt);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
                con.commit();
                return receipt;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        }
    }

    public DefaultTableModel getAllPayments() throws Exception {
        return table(SELECT_ALL + "ORDER BY p.payment_id DESC", HEADERS);
    }

    /** Builds a printable receipt text for a receipt number. */
    public String buildReceipt(String receiptNo) throws Exception {
        String sql = "SELECT p.receipt_no, p.amount, p.method, p.payment_date, b.booking_id, b.booking_code, "
                + "c.name, c.phone, r.room_number, r.room_type, b.check_in, b.check_out, "
                + "COALESCE(b.final_bill, b.total_amount) AS bill FROM payments p "
                + "JOIN bookings b ON p.booking_id=b.booking_id JOIN customers c ON b.customer_id=c.customer_id "
                + "JOIN rooms r ON b.room_id=r.room_id WHERE p.receipt_no=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, receiptNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Receipt not found.");
                double paidTotal = totalPaid(rs.getInt("booking_id"));
                double bill = rs.getDouble("bill");
                String line = "==========================================\n";
                return line
                        + "          HOTEL PAYMENT RECEIPT\n"
                        + line
                        + String.format("Receipt No   : %s%n", rs.getString("receipt_no"))
                        + String.format("Date         : %s%n", rs.getTimestamp("payment_date").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
                        + String.format("Booking ID   : %s%n", rs.getString("booking_code"))
                        + String.format("Customer     : %s%n", rs.getString("name"))
                        + String.format("Phone        : %s%n", rs.getString("phone"))
                        + String.format("Room         : %s (%s)%n", rs.getString("room_number"), rs.getString("room_type"))
                        + String.format("Stay         : %s to %s%n", rs.getDate("check_in"), rs.getDate("check_out"))
                        + "------------------------------------------\n"
                        + String.format("Bill Amount  : Rs. %.2f%n", bill)
                        + String.format("Paid Now     : Rs. %.2f (%s)%n", rs.getDouble("amount"), rs.getString("method"))
                        + String.format("Total Paid   : Rs. %.2f%n", paidTotal)
                        + String.format("Balance Due  : Rs. %.2f%n", Math.max(0, bill - paidTotal))
                        + line
                        + "       Thank you! Please visit again.\n";
            }
        }
    }

    /** Current timestamp text (used in file names). */
    public static String stamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
