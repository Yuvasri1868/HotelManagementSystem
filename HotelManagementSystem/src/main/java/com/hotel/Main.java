package com.hotel;

import com.hotel.db.DatabaseInitializer;
import com.hotel.ui.LoginFrame;

import javax.swing.*;

/** Application entry point. Run this class. */
public class Main {
    public static void main(String[] args) {
        try {
            // Cross-platform look so button colors display the same on Windows/Mac/Linux
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }

        try {
            DatabaseInitializer.init();   // creates DB + tables + default admin on first run
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Could not connect to MySQL.\n\n"
                            + "1) Make sure MySQL server is running.\n"
                            + "2) Open DBConnection.java and set the correct DB_USER / DB_PASSWORD.\n\n"
                            + "Details: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
