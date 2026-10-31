package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.dao.RoomDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Reports: booking history, rooms, customers and payments. Each can be exported to CSV. */
public class ReportPanel extends JPanel {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    private final JTable tblBookings = UITheme.table(new javax.swing.table.DefaultTableModel());
    private final JTable tblAvailable = UITheme.table(new javax.swing.table.DefaultTableModel());
    private final JTable tblOccupied = UITheme.table(new javax.swing.table.DefaultTableModel());
    private final JTable tblCustomers = UITheme.table(new javax.swing.table.DefaultTableModel());
    private final JTable tblPayments = UITheme.table(new javax.swing.table.DefaultTableModel());
    private final JLabel lblRevenue = new JLabel("Total collected: Rs. 0.00");
    private final JTabbedPane tabs = new JTabbedPane();

    public ReportPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        add(UITheme.titleBar("Reports"), BorderLayout.NORTH);

        tabs.setFont(UITheme.BOLD);
        tabs.addTab("Booking History", UITheme.scroll(tblBookings));
        tabs.addTab("Available Rooms", UITheme.scroll(tblAvailable));
        tabs.addTab("Occupied / Booked Rooms", UITheme.scroll(tblOccupied));
        tabs.addTab("Customers", UITheme.scroll(tblCustomers));
        tabs.addTab("Payments", UITheme.scroll(tblPayments));

        JButton btnRefresh = UITheme.button("Refresh", UITheme.INFO);
        JButton btnExport = UITheme.button("Export Current Tab to CSV", UITheme.SUCCESS);
        lblRevenue.setFont(UITheme.BOLD);
        lblRevenue.setForeground(UITheme.SUCCESS);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(UITheme.BG);
        bar.add(btnRefresh); bar.add(btnExport); bar.add(lblRevenue);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(UITheme.BG);
        center.setBorder(new EmptyBorder(10, 20, 20, 20));
        center.add(bar, BorderLayout.NORTH);
        center.add(tabs, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> refresh());
        btnExport.addActionListener(e -> {
            JTable[] all = {tblBookings, tblAvailable, tblOccupied, tblCustomers, tblPayments};
            UITheme.exportCsv(this, all[tabs.getSelectedIndex()].getModel());
        });
        refresh();
    }

    public void refresh() {
        try {
            tblBookings.setModel(bookingDAO.getAllBookings());
            tblAvailable.setModel(roomDAO.getRoomsByStatus("Available"));
            tblOccupied.setModel(roomDAO.getUnavailableRooms());
            tblCustomers.setModel(customerDAO.getAllCustomers());
            tblPayments.setModel(paymentDAO.getAllPayments());
            double sum = 0;
            for (int i = 0; i < tblPayments.getModel().getRowCount(); i++)
                sum += Double.parseDouble(tblPayments.getModel().getValueAt(i, 4).toString());
            lblRevenue.setText(String.format("Total collected: Rs. %.2f", sum));
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }
}
