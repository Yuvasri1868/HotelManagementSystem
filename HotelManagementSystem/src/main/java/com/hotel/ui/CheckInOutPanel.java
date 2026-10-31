package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.PaymentDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Check-in and check-out of registered customers' bookings. */
public class CheckInOutPanel extends JPanel {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final JTable table = UITheme.table(new javax.swing.table.DefaultTableModel());

    public CheckInOutPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        add(UITheme.titleBar("Check-In / Check-Out"), BorderLayout.NORTH);

        JLabel hint = new JLabel("Select a booking: 'Booked' bookings can be checked in; 'Checked-In' guests can be checked out.");
        hint.setForeground(UITheme.GRAY);
        JButton btnIn = UITheme.button("Check-In", UITheme.SUCCESS);
        JButton btnOut = UITheme.button("Check-Out & Final Bill", UITheme.WARN);
        JButton btnRefresh = UITheme.button("Refresh", UITheme.GRAY);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(UITheme.BG);
        bar.add(btnIn); bar.add(btnOut); bar.add(btnRefresh);

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 8));
        top.setBackground(UITheme.BG);
        top.add(bar);
        top.add(hint);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(UITheme.BG);
        center.setBorder(new EmptyBorder(10, 20, 20, 20));
        center.add(top, BorderLayout.NORTH);
        center.add(UITheme.scroll(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        btnIn.addActionListener(e -> checkIn());
        btnOut.addActionListener(e -> checkOut());
        btnRefresh.addActionListener(e -> refresh());
        refresh();
    }

    private void checkIn() {
        Object id = UITheme.selected(table, 0);
        if (id == null) { UITheme.error(this, "Select a booking first."); return; }
        try {
            bookingDAO.checkIn(Integer.parseInt(id.toString()));
            UITheme.info(this, "Guest checked in. Room is now Occupied.");
            refresh();
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    private void checkOut() {
        Object id = UITheme.selected(table, 0);
        if (id == null) { UITheme.error(this, "Select a booking first."); return; }
        if (!UITheme.confirm(this, "Check-out the selected guest?")) return;
        try {
            int bookingId = Integer.parseInt(id.toString());
            double bill = bookingDAO.checkOut(bookingId);
            double paid = paymentDAO.totalPaid(bookingId);
            UITheme.info(this, String.format("Check-out complete. Room is now Available.%n%n"
                    + "Final Bill : Rs. %.2f%nPaid So Far: Rs. %.2f%nBalance Due: Rs. %.2f%n%n"
                    + "Use the Payments module to collect the balance.", bill, paid, Math.max(0, bill - paid)));
            refresh();
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    public void refresh() {
        try {
            table.setModel(bookingDAO.getActiveBookings());
            table.getColumnModel().getColumn(0).setMaxWidth(50);
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }
}
