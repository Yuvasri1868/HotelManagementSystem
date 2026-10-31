package com.hotel.ui;

import com.hotel.dao.RoomDAO;
import com.hotel.model.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Add / update / delete / view rooms. */
public class RoomPanel extends JPanel {
    private final RoomDAO dao = new RoomDAO();
    private final JTextField txtNumber = UITheme.field();
    private final JComboBox<String> cmbType = UITheme.combo(new String[]{"Single", "Double", "Deluxe", "Suite"});
    private final JTextField txtPrice = UITheme.field();
    private final JTextField txtCapacity = UITheme.field();
    private final JComboBox<String> cmbStatus = UITheme.combo(new String[]{"Available", "Booked", "Occupied"});
    private final JTable table = UITheme.table(new javax.swing.table.DefaultTableModel());
    private int selectedId = -1;

    public RoomPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        add(UITheme.titleBar("Room Management"), BorderLayout.NORTH);

        JPanel form = UITheme.formPanel("Room Details");
        UITheme.addRow(form, 0, "Room Number", txtNumber);
        UITheme.addRow(form, 1, "Room Type", cmbType);
        UITheme.addRow(form, 2, "Price / Night", txtPrice);
        UITheme.addRow(form, 3, "Capacity", txtCapacity);
        UITheme.addRow(form, 4, "Status", cmbStatus);

        JButton btnAdd = UITheme.button("Add Room", UITheme.SUCCESS);
        JButton btnUpdate = UITheme.button("Update Room", UITheme.INFO);
        JButton btnDelete = UITheme.button("Delete Room", UITheme.DANGER);
        JButton btnClear = UITheme.button("Clear", UITheme.GRAY);
        JPanel btns = new JPanel(new GridLayout(4, 1, 0, 8));
        btns.setOpaque(false);
        btns.add(btnAdd); btns.add(btnUpdate); btns.add(btnDelete); btns.add(btnClear);
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

        // clicking a row loads it into the form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) return;
            selectedId = Integer.parseInt(UITheme.selected(table, 0).toString());
            txtNumber.setText(String.valueOf(UITheme.selected(table, 1)));
            cmbType.setSelectedItem(String.valueOf(UITheme.selected(table, 2)));
            txtPrice.setText(String.valueOf(UITheme.selected(table, 3)));
            txtCapacity.setText(String.valueOf(UITheme.selected(table, 4)));
            cmbStatus.setSelectedItem(String.valueOf(UITheme.selected(table, 5)));
        });

        btnAdd.addActionListener(e -> save(true));
        btnUpdate.addActionListener(e -> save(false));
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> clear());
        refresh();
    }

    /** Reads and validates the form. Returns null if invalid. */
    private Room readForm() {
        String number = txtNumber.getText().trim();
        String type = String.valueOf(cmbType.getSelectedItem());
        String status = String.valueOf(cmbStatus.getSelectedItem());
        if (number.isEmpty()) { UITheme.error(this, "Room number is required."); return null; }
        double price;
        int capacity;
        try {
            price = Double.parseDouble(txtPrice.getText().trim());
            capacity = Integer.parseInt(txtCapacity.getText().trim());
        } catch (NumberFormatException ex) {
            UITheme.error(this, "Price must be a number and capacity must be a whole number.");
            return null;
        }
        if (price <= 0 || capacity <= 0) { UITheme.error(this, "Price and capacity must be greater than 0."); return null; }
        return new Room(selectedId, number, type, price, capacity, status);
    }

    private void save(boolean isNew) {
        if (!isNew && selectedId < 0) { UITheme.error(this, "Select a room from the table first."); return; }
        Room r = readForm();
        if (r == null) return;
        try {
            if (isNew) dao.addRoom(r); else dao.updateRoom(r);
            UITheme.info(this, isNew ? "Room added successfully." : "Room updated successfully.");
            clear();
            refresh();
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    private void delete() {
        if (selectedId < 0) { UITheme.error(this, "Select a room to delete."); return; }
        if (!UITheme.confirm(this, "Delete the selected room?")) return;
        try {
            dao.deleteRoom(selectedId);
            UITheme.info(this, "Room deleted.");
            clear();
            refresh();
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    private void clear() {
        selectedId = -1;
        txtNumber.setText("");
        txtPrice.setText("");
        txtCapacity.setText("");
        cmbType.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        table.clearSelection();
    }

    public void refresh() {
        try {
            table.setModel(dao.getAllRooms());
            table.getColumnModel().getColumn(0).setMaxWidth(50);
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }
}
