package com.hotel.ui;

import com.hotel.dao.AdminDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Admin login screen. */
public class LoginFrame extends JFrame {
    private final JTextField txtUser = UITheme.field();
    private final JPasswordField txtPass = new JPasswordField();
    private final AdminDAO adminDAO = new AdminDAO();

    public LoginFrame() {
        setTitle("Hotel Room Management System - Login");
        setSize(820, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new GridLayout(1, 2));

        // ---- Left banner ----
        JPanel left = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY, 0, getHeight(), UITheme.PRIMARY_LIGHT));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        JLabel icon = new JLabel("\u2605 \u2605 \u2605 \u2605 \u2605", SwingConstants.CENTER);
        icon.setForeground(UITheme.ACCENT);
        icon.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel name = new JLabel("<html><div style='text-align:center'>HOTEL<br>ROOM MANAGEMENT</div></html>", SwingConstants.CENTER);
        name.setForeground(Color.WHITE);
        name.setFont(new Font("SansSerif", Font.BOLD, 28));
        JLabel tag = new JLabel("Comfort. Care. Control.", SwingConstants.CENTER);
        tag.setForeground(new Color(200, 210, 225));
        tag.setFont(new Font("SansSerif", Font.ITALIC, 14));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.insets = new Insets(8, 8, 8, 8);
        g.gridy = 0; left.add(icon, g);
        g.gridy = 1; left.add(name, g);
        g.gridy = 2; left.add(tag, g);

        // ---- Right form ----
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(new EmptyBorder(20, 40, 20, 40));
        JLabel title = new JLabel("Admin Login");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);

        txtPass.setFont(UITheme.FONT);
        txtPass.setPreferredSize(new Dimension(220, 34));
        txtUser.setPreferredSize(new Dimension(220, 34));
        JButton btnLogin = UITheme.button("LOGIN", UITheme.PRIMARY);
        JButton btnExit = UITheme.button("EXIT", UITheme.GRAY);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(6, 0, 6, 0);
        c.gridy = 0; right.add(title, c);
        c.gridy = 1; right.add(UITheme.label("Username"), c);
        c.gridy = 2; right.add(txtUser, c);
        c.gridy = 3; right.add(UITheme.label("Password"), c);
        c.gridy = 4; right.add(txtPass, c);
        c.gridy = 5; c.insets = new Insets(18, 0, 6, 0); right.add(btnLogin, c);
        c.gridy = 6; c.insets = new Insets(0, 0, 6, 0); right.add(btnExit, c);
        JLabel hint = new JLabel("Default login: admin / admin123");
        hint.setForeground(UITheme.GRAY);
        c.gridy = 7; right.add(hint, c);

        add(left);
        add(right);

        btnLogin.addActionListener(e -> doLogin());
        txtPass.addActionListener(e -> doLogin());   // Enter key
        btnExit.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(btnLogin);
    }

    /** Validates credentials against MySQL and opens the dashboard. */
    private void doLogin() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            UITheme.error(this, "Please enter username and password.");
            return;
        }
        try {
            String fullName = adminDAO.login(username, password);
            if (fullName == null) {
                UITheme.error(this, "Invalid username or password.");
                txtPass.setText("");
            } else {
                new DashboardFrame(fullName).setVisible(true);
                dispose();
            }
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }
}
