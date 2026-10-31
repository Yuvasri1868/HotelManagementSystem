package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Book rooms, auto-calculate charges, prevent double booking, cancel bookings. */
public class BookingPanel extends JPanel {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    private final JComboBox<Customer> cmbCustomer = new JComboBox<>();
    private final JComboBox<Room> cmbRoom = new JComboBox<>();
    private final JTextField txtCheckIn = UITheme.field();
    private final JTextField txtCheckOut = UITheme.field();
    private final JLabel lblTotal = new JLabel("Total: Rs. 0.00");
    private final JTable table = UITheme.table(new javax.swing.table.DefaultTableModel());

    public BookingPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        add(UITheme.titleBar("Booking Management"), BorderLayout.NORTH);

        cmbCustomer.setFont(UITheme.FONT);
        cmbRoom.setFont(UITheme.FONT);
        cmbCustomer.setPreferredSize(new Dimension(230, 32));
        cmbRoom.setPreferredSize(new Dimension(230, 32));
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTotal.setForeground(UITheme.SUCCESS);

        JPanel form = UITheme.formPanel("New Booking");
        UITheme.addRow(form, 0, "Customer", cmbCustomer);
        UITheme.addRow(form, 1, "Room", cmbRoom);
        UITheme.addRow(form, 2, "Check-In (yyyy-MM-dd)", txtCheckIn);
        UITheme.addRow(form, 3, "Check-Out (yyyy-MM-dd)", txtCheckOut);
        UITheme.addWide(form, 4, lblTotal);
        JButton btnCalc = UITheme.button("Calculate Total", UITheme.INFO);
        JButton btnBook = UITheme.button("Book Room", UITheme.SUCCESS);
        JButton btnCancel = UITheme.button("Cancel Selected Booking", UITheme.DANGER);
        JButton btnRefresh = UITheme.button("Refresh", UITheme.GRAY);
        JPanel btns = new JPanel(new GridLayout(4, 1, 0, 8));
        btns.setOpaque(false);
        btns.add(btnCalc); btns.add(btnBook); btns.add(btnCancel); btns.add(btnRefresh);
        UITheme.addWide(form, 5, btns);

        JPanel west = new JPanel(new BorderLayout());
        west.setBackground(UITheme.BG);
        west.setBorder(new EmptyBorder(10, 20, 20, 10));
        west.add(form, BorderLayout.NORTH);
        add(west, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.BG);
        center.setBorder(new EmptyBorder(10, 10, 20, 20));
        center.add(UITheme.scroll(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        btnCalc.addActionListener(e -> calculate());
        btnBook.addActionListener(e -> book());
        btnCancel.addActionListener(e -> cancelBooking());
        btnRefresh.addActionListener(e -> refresh());
        refresh();
    }

    /** Parses both dates; returns null (and shows a message) if invalid. */
    private LocalDate[] readDates() {
        try {
            LocalDate in = LocalDate.parse(txtCheckIn.getText().trim());
            LocalDate out = LocalDate.parse(txtCheckOut.getText().trim());
            if (!out.isAfter(in)) {
                UITheme.error(this, "Check-out date must be after the check-in date.");
                return null;
            }
            return new LocalDate[]{in, out};
        } catch (DateTimeParseException ex) {
            UITheme.error(this, "Enter dates in the format yyyy-MM-dd (example: 2026-11-05).");
            return null;
        }
    }

    private double calculate() {
        Room room = (Room) cmbRoom.getSelectedItem();
        if (room == null) { UITheme.error(this, "No available room selected."); return -1; }
        LocalDate[] d = readDates();
        if (d == null) return -1;
        double total = BookingDAO.nights(d[0], d[1]) * room.getPrice();
        lblTotal.setText(String.format("Total: Rs. %.2f (%d nights)", total, BookingDAO.nights(d[0], d[1])));
        return total;
    }

    private void book() {
        Customer customer = (Customer) cmbCustomer.getSelectedItem();
        Room room = (Room) cmbRoom.getSelectedItem();
        if (customer == null) { UITheme.error(this, "Please register a customer first."); return; }
        if (room == null) { UITheme.error(this, "No rooms are available."); return; }
        LocalDate[] d = readDates();
        if (d == null) return;
        if (d[0].isBefore(LocalDate.now())) { UITheme.error(this, "Check-in date cannot be in the past."); return; }
        try {
            Booking b = new Booking();
            b.setCustomerId(customer.getCustomerId());
            b.setRoomId(room.getRoomId());
            b.setCheckIn(d[0]);
            b.setCheckOut(d[1]);
            String code = bookingDAO.createBooking(b, room.getPrice());
            UITheme.info(this, "Booking successful!\nBooking ID: " + code + "\n" + lblTotal.getText().replace("Total", "Total charges"));
            refresh();
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    private void cancelBooking() {
        Object id = UITheme.selected(table, 0);
        if (id == null) { UITheme.error(this, "Select a booking from the table."); return; }
        if (!UITheme.confirm(this, "Cancel the selected booking?")) return;
        try {
            bookingDAO.cancel(Integer.parseInt(id.toString()));
            UITheme.info(this, "Booking cancelled. Room is available again.");
            refresh();
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    /** Reloads customers, available rooms and the bookings table. */
    public void refresh() {
        try {
            cmbCustomer.removeAllItems();
            List<Customer> customers = customerDAO.getAll();
            for (Customer c : customers) cmbCustomer.addItem(c);
            cmbRoom.removeAllItems();
            for (Room r : roomDAO.getAvailableRooms()) cmbRoom.addItem(r);
            if (txtCheckIn.getText().isEmpty()) {
                txtCheckIn.setText(LocalDate.now().toString());
                txtCheckOut.setText(LocalDate.now().plusDays(1).toString());
            }
            table.setModel(bookingDAO.getAllBookings());
            table.getColumnModel().getColumn(0).setMaxWidth(50);
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }
}
