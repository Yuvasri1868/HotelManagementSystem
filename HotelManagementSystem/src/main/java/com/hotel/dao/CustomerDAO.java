package com.hotel.dao;

import com.hotel.db.DBConnection;
import com.hotel.model.Customer;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** All database operations for customers. */
public class CustomerDAO extends BaseDAO {
    private static final String[] HEADERS = {"ID", "Name", "Phone", "Email", "Address", "ID Proof"};
    private static final String COLS = "customer_id, name, phone, email, address, id_proof";

    public void addCustomer(Customer c) throws Exception {
        String sql = "INSERT INTO customers(name,phone,email,address,id_proof) VALUES(?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getIdProof());
            ps.executeUpdate();
        }
    }

    public void updateCustomer(Customer c) throws Exception {
        String sql = "UPDATE customers SET name=?, phone=?, email=?, address=?, id_proof=? WHERE customer_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getIdProof());
            ps.setInt(6, c.getCustomerId());
            ps.executeUpdate();
        }
    }

    public void deleteCustomer(int id) throws Exception {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM customers WHERE customer_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public DefaultTableModel getAllCustomers() throws Exception {
        return table("SELECT " + COLS + " FROM customers ORDER BY customer_id DESC", HEADERS);
    }

    /** Search by name, phone, email or ID proof. */
    public DefaultTableModel search(String keyword) throws Exception {
        String like = "%" + keyword + "%";
        return table("SELECT " + COLS + " FROM customers WHERE name LIKE ? OR phone LIKE ? OR email LIKE ? OR id_proof LIKE ? ORDER BY customer_id DESC",
                HEADERS, like, like, like, like);
    }

    /** Customers for combo boxes. */
    public List<Customer> getAll() throws Exception {
        List<Customer> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT " + COLS + " FROM customers ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Customer(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)));
            }
        }
        return list;
    }
}
