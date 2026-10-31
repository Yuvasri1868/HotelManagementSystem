package com.hotel.ui;

import com.hotel.dao.CustomerDAO;
import com.hotel.model.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Register / update / delete / search customers. */
public class CustomerPanel extends JPanel {
    private final CustomerDAO dao = new CustomerDAO();
    private final JTextField txtName = UITheme.field();
    private final JTextField txtPhone = UITheme.field();
    private final JTextField txtEmail = UITheme.field();
    private final JTextField txtAddress = UITheme.field();
    private final JTextField txtIdProof = UITheme.field();
    private final JTextField txtSearch = UITheme.field();
    private final JTable table = UITheme.table(new javax.swing.table.DefaultTableModel());
    private int selectedId = -1;

    public CustomerPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        add(UITheme.titleBar("Customer Management"), BorderLayout.NORTH);

        JPanel form = UITheme.formPanel("Customer Details");
        UITheme.addRow(form, 0, "Name", txtName);
        UITheme.addRow(form, 1, "Phone", txtPhone);
        UITheme.addRow(form, 2, "Email", txtEmail);
        UITheme.addRow(form, 3, "Address", txtAddress);
        UITheme.addRow(form, 4, "ID Proof (No.)", txtIdProof);
        JButton btnAdd = UITheme.button("Register Customer", UITheme.SUCCESS);
        JButton btnUpdate = UITheme.button("Update Customer", UITheme.INFO);
        JButton btnDelete = UITheme.button("Delete Customer", UITheme.DANGER);
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

        // search bar + table
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setBackground(UITheme.BG);
        JButton btnSearch = UITheme.button("Search", UITheme.INFO);
        JButton btnAll = UITheme.button("Show All", UITheme.GRAY);
        searchBar.add(UITheme.label("Search (name / phone / email / ID):"));
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);
        searchBar.add(btnAll);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(UITheme.BG);
        center.setBorder(new EmptyBorder(10, 10, 20, 20));
        center.add(searchBar, BorderLayout.NORTH);
        center.add(UITheme.scroll(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) return;
            selectedId = Integer.parseInt(UITheme.selected(table, 0).toString());
            txtName.setText(str(UITheme.selected(table, 1)));
            txtPhone.setText(str(UITheme.selected(table, 2)));
            txtEmail.setText(str(UITheme.selected(table, 3)));
            txtAddress.setText(str(UITheme.selected(table, 4)));
            txtIdProof.setText(str(UITheme.selected(table, 5)));
        });

        btnAdd.addActionListener(e -> save(true));
        btnUpdate.addActionListener(e -> save(false));
        btnDelete.addActionListener(e -> delete());
        btnClear.addActionListener(e -> clear());
        btnSearch.addActionListener(e -> search());
        txtSearch.addActionListener(e -> search());
        btnAll.addActionListener(e -> { txtSearch.setText(""); refresh(); });
        refresh();
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    private void save(boolean isNew) {
        if (!isNew && selectedId < 0) { UITheme.error(this, "Select a customer from the table first."); return; }
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        if (name.isEmpty() || phone.isEmpty()) { UITheme.error(this, "Name and phone are required."); return; }
        if (!phone.matches("[0-9+\\- ]{7,15}")) { UITheme.error(this, "Enter a valid phone number (7-15 digits)."); return; }
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            UITheme.error(this, "Enter a valid email address.");
            return;
        }
        Customer c = new Customer(selectedId, name, phone, email, txtAddress.getText().trim(), txtIdProof.getText().trim());
        try {
            if (isNew) dao.addCustomer(c); else dao.updateCustomer(c);
            UITheme.info(this, isNew ? "Customer registered." : "Customer updated.");
            clear();
            refresh();
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    private void delete() {
        if (selectedId < 0) { UITheme.error(this, "Select a customer to delete."); return; }
        if (!UITheme.confirm(this, "Delete the selected customer?")) return;
        try {
            dao.deleteCustomer(selectedId);
            UITheme.info(this, "Customer deleted.");
            clear();
            refresh();
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    private void search() {
        try {
            table.setModel(dao.search(txtSearch.getText().trim()));
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    private void clear() {
        selectedId = -1;
        txtName.setText(""); txtPhone.setText(""); txtEmail.setText("");
        txtAddress.setText(""); txtIdProof.setText("");
        table.clearSelection();
    }

    public void refresh() {
        try {
            table.setModel(dao.getAllCustomers());
            table.getColumnModel().getColumn(0).setMaxWidth(50);
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }
}
