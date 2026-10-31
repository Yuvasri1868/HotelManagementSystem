package com.hotel.dao;

import com.hotel.db.DBConnection;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

/** Base class for all DAOs: shared helper to load a query into a (read-only) table model. */
public class BaseDAO {

    /** Runs a SELECT with PreparedStatement parameters and returns a table model with the given headers. */
    public static DefaultTableModel table(String sql, String[] headers, Object... params) throws Exception {
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int c = 0; c < cols; c++) row[c] = rs.getObject(c + 1);
                    model.addRow(row);
                }
            }
        }
        return model;
    }

    /** Runs a SELECT COUNT(*) style query and returns the first number. */
    public static int count(String sql, Object... params) throws Exception {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
