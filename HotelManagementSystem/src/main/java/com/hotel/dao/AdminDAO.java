package com.hotel.dao;

import com.hotel.db.DBConnection;
import com.hotel.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Login validation against the admin table. */
public class AdminDAO extends BaseDAO {

    /** Returns the admin's full name if credentials are valid, otherwise null. */
    public String login(String username, String password) throws Exception {
        String sql = "SELECT full_name FROM admin WHERE username=? AND password=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, Util.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("full_name") : null;
            }
        }
    }
}
