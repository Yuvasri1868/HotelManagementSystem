package com.hotel.ui;

import com.hotel.dao.DashboardDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Main window: sidebar navigation + content area (CardLayout). */
public class DashboardFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    private final JLabel lblTotalRooms = new JLabel("0");
    private final JLabel lblAvailable = new JLabel("0");
    private final JLabel lblOccupied = new JLabel("0");
    private final JLabel lblBooked = new JLabel("0");
    private final JLabel lblCustomers = new JLabel("0");
    private final JLabel lblBookings = new JLabel("0");

    private final RoomPanel roomPanel = new RoomPanel();
    private final CustomerPanel customerPanel = new CustomerPanel();
    private final BookingPanel bookingPanel = new BookingPanel();
    private final CheckInOutPanel checkPanel = new CheckInOutPanel();
    private final PaymentPanel paymentPanel = new PaymentPanel();
    private final ReportPanel reportPanel = new ReportPanel();

    public DashboardFrame(String adminName) {
        setTitle("Hotel Room Management System");
        setSize(1200, 720);
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ---------- Sidebar ----------
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(UITheme.PRIMARY);
        side.setPreferredSize(new Dimension(220, 0));
        side.setBorder(new EmptyBorder(15, 10, 15, 10));
        JLabel brand = new JLabel("<html><center>HOTEL<br><span style='color:#D4A017'>MANAGER</span></center></html>", SwingConstants.CENTER);
        brand.setFont(new Font("SansSerif", Font.BOLD, 24));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(brand);
        side.add(Box.createVerticalStrut(25));

        addNav(side, "Dashboard", "home", null);
        addNav(side, "Rooms", "rooms", () -> roomPanel.refresh());
        addNav(side, "Customers", "customers", () -> customerPanel.refresh());
        addNav(side, "Bookings", "bookings", () -> bookingPanel.refresh());
        addNav(side, "Check-In / Out", "check", () -> checkPanel.refresh());
        addNav(side, "Payments", "payments", () -> paymentPanel.refresh());
        addNav(side, "Reports", "reports", () -> reportPanel.refresh());
        side.add(Box.createVerticalGlue());

        JButton logout = UITheme.button("Logout", UITheme.DANGER);
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.setMaximumSize(new Dimension(200, 40));
        logout.addActionListener(e -> {
            if (UITheme.confirm(this, "Do you want to logout?")) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        side.add(logout);

        // ---------- Header ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel welcome = new JLabel("Welcome, " + adminName);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 16));
        welcome.setForeground(UITheme.PRIMARY);
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        date.setForeground(UITheme.GRAY);
        header.add(welcome, BorderLayout.WEST);
        header.add(date, BorderLayout.EAST);

        // ---------- Content ----------
        content.setBackground(UITheme.BG);
        content.add(buildHome(), "home");
        content.add(roomPanel, "rooms");
        content.add(customerPanel, "customers");
        content.add(bookingPanel, "bookings");
        content.add(checkPanel, "check");
        content.add(paymentPanel, "payments");
        content.add(reportPanel, "reports");

        add(side, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        refreshStats();
    }

    /** Creates a sidebar navigation button. */
    private void addNav(JPanel side, String text, String card, Runnable onShow) {
        JButton b = UITheme.button(text, UITheme.PRIMARY_LIGHT);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(200, 42));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.addActionListener(e -> {
            if ("home".equals(card)) refreshStats();
            if (onShow != null) onShow.run();
            cards.show(content, card);
        });
        side.add(b);
        side.add(Box.createVerticalStrut(8));
    }

    /** Home screen with statistic cards. */
    private JPanel buildHome() {
        JPanel home = new JPanel(new BorderLayout());
        home.setBackground(UITheme.BG);
        home.add(UITheme.titleBar("Dashboard"), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 20, 20));
        grid.setBackground(UITheme.BG);
        grid.setBorder(new EmptyBorder(20, 20, 20, 20));
        grid.add(statCard("Total Rooms", lblTotalRooms, UITheme.INFO));
        grid.add(statCard("Available Rooms", lblAvailable, UITheme.SUCCESS));
        grid.add(statCard("Occupied Rooms", lblOccupied, UITheme.DANGER));
        grid.add(statCard("Reserved (Booked)", lblBooked, UITheme.WARN));
        grid.add(statCard("Total Customers", lblCustomers, UITheme.PRIMARY_LIGHT));
        grid.add(statCard("Total Bookings", lblBookings, new Color(142, 68, 173)));
        home.add(grid, BorderLayout.CENTER);

        JButton refresh = UITheme.button("Refresh", UITheme.INFO);
        refresh.addActionListener(e -> refreshStats());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(UITheme.BG);
        south.add(refresh);
        home.add(south, BorderLayout.SOUTH);
        return home;
    }

    private JPanel statCard(String title, JLabel value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(new EmptyBorder(20, 25, 20, 25));
        JLabel t = new JLabel(title);
        t.setForeground(Color.WHITE);
        t.setFont(new Font("SansSerif", Font.BOLD, 17));
        value.setForeground(Color.WHITE);
        value.setFont(new Font("SansSerif", Font.BOLD, 54));
        card.add(t, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    /** Reloads dashboard numbers from MySQL. */
    public void refreshStats() {
        try {
            lblTotalRooms.setText(String.valueOf(dashboardDAO.totalRooms()));
            lblAvailable.setText(String.valueOf(dashboardDAO.availableRooms()));
            lblOccupied.setText(String.valueOf(dashboardDAO.occupiedRooms()));
            lblBooked.setText(String.valueOf(dashboardDAO.bookedRooms()));
            lblCustomers.setText(String.valueOf(dashboardDAO.totalCustomers()));
            lblBookings.setText(String.valueOf(dashboardDAO.totalBookings()));
        } catch (Exception e) {
            UITheme.dbError(this, e);
        }
    }
}
