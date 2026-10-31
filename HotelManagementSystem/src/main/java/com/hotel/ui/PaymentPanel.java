package com.hotel.ui;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.PaymentDAO;
import com.hotel.model.Payment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;

/** Record payments (Cash / UPI / Card), view history and receipts. */
public class PaymentPanel extends JPanel {
    private final BookingDAO bookingDAO = new BookingDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    private final JTable bookingTable = UITheme.table(new javax.swing.table.DefaultTableModel());
    private final JTable paymentTable = UITheme.table(new javax.swing.table.DefaultTableModel());
    private final JLabel lblBooking = new JLabel("-");
    private final JLabel lblBill = new JLabel("Rs. 0.00");
    private final JLabel lblPaid = new JLabel("Rs. 0.00");
    private final JLabel lblBalance = new JLabel("Rs. 0.00");
    private final JTextField txtAmount = UITheme.field();
    private final JComboBox<String> cmbMethod = UITheme.combo(new String[]{"Cash", "UPI", "Card"});
    private int selectedBookingId = -1;
    private double balance = 0;

    public PaymentPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        add(UITheme.titleBar("Payment Management"), BorderLayout.NORTH);

        // ---- right: payment form ----
        JPanel form = UITheme.formPanel("Record Payment");
        UITheme.addRow(form, 0, "Booking", lblBooking);
        UITheme.addRow(form, 1, "Bill Amount", lblBill);
        UITheme.addRow(form, 2, "Already Paid", lblPaid);
        UITheme.addRow(form, 3, "Balance Due", lblBalance);
        UITheme.addRow(form, 4, "Amount to Pay", txtAmount);
        UITheme.addRow(form, 5, "Method", cmbMethod);
        lblBalance.setForeground(UITheme.DANGER);
        JButton btnPay = UITheme.button("Record Payment", UITheme.SUCCESS);
        JButton btnReceipt = UITheme.button("View Receipt (selected payment)", UITheme.INFO);
        JButton btnRefresh = UITheme.button("Refresh", UITheme.GRAY);
        JPanel btns = new JPanel(new GridLayout(3, 1, 0, 8));
        btns.setOpaque(false);
        btns.add(btnPay); btns.add(btnReceipt); btns.add(btnRefresh);
        UITheme.addWide(form, 6, btns);

        JPanel east = new JPanel(new BorderLayout());
        east.setBackground(UITheme.BG);
        east.setBorder(new EmptyBorder(10, 10, 20, 20));
        east.add(form, BorderLayout.NORTH);
        east.setPreferredSize(new Dimension(360, 0));
        add(east, BorderLayout.EAST);

        // ---- center: two tables ----
        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setBackground(UITheme.BG);
        top.add(UITheme.label("1) Select a booking"), BorderLayout.NORTH);
        top.add(UITheme.scroll(bookingTable), BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottom.setBackground(UITheme.BG);
        bottom.add(UITheme.label("Payment history"), BorderLayout.NORTH);
        bottom.add(UITheme.scroll(paymentTable), BorderLayout.CENTER);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        split.setResizeWeight(0.5);
        split.setBorder(null);
        split.setBackground(UITheme.BG);
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UITheme.BG);
        center.setBorder(new EmptyBorder(10, 20, 20, 10));
        center.add(split, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedBooking();
        });
        btnPay.addActionListener(e -> recordPayment());
        btnReceipt.addActionListener(e -> viewSelectedReceipt());
        btnRefresh.addActionListener(e -> refresh());
        refresh();
    }

    /** Fills the bill / paid / balance labels for the selected booking. */
    private void loadSelectedBooking() {
        Object id = UITheme.selected(bookingTable, 0);
        if (id == null) return;
        try {
            selectedBookingId = Integer.parseInt(id.toString());
            double bill = bookingDAO.getBillAmount(selectedBookingId);
            double paid = paymentDAO.totalPaid(selectedBookingId);
            balance = Math.max(0, bill - paid);
            lblBooking.setText(String.valueOf(UITheme.selected(bookingTable, 1)));
            lblBill.setText(String.format("Rs. %.2f", bill));
            lblPaid.setText(String.format("Rs. %.2f", paid));
            lblBalance.setText(String.format("Rs. %.2f", balance));
            txtAmount.setText(String.format("%.2f", balance));
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    private void recordPayment() {
        if (selectedBookingId < 0) { UITheme.error(this, "Select a booking first."); return; }
        if ("Cancelled".equals(String.valueOf(UITheme.selected(bookingTable, 9)))) {
            UITheme.error(this, "Payments cannot be recorded for a cancelled booking.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(txtAmount.getText().trim());
        } catch (NumberFormatException ex) {
            UITheme.error(this, "Enter a valid amount.");
            return;
        }
        if (amount <= 0) { UITheme.error(this, "Amount must be greater than 0."); return; }
        if (amount > balance + 0.001) {
            UITheme.error(this, String.format("Amount is more than the balance due (Rs. %.2f).", balance));
            return;
        }
        try {
            Payment p = new Payment();
            p.setBookingId(selectedBookingId);
            p.setAmount(amount);
            p.setMethod(String.valueOf(cmbMethod.getSelectedItem()));
            String receiptNo = paymentDAO.addPayment(p);
            refresh();
            showReceipt(receiptNo);
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    private void viewSelectedReceipt() {
        Object r = UITheme.selected(paymentTable, 1);
        if (r == null) { UITheme.error(this, "Select a payment from the history table."); return; }
        showReceipt(r.toString());
    }

    /** Shows the receipt in a dialog with Save and Print options. */
    private void showReceipt(String receiptNo) {
        try {
            String text = paymentDAO.buildReceipt(receiptNo);
            JTextArea area = new JTextArea(text, 18, 42);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            area.setEditable(false);
            Object[] options = {"Close", "Save as Text File", "Print"};
            int choice = JOptionPane.showOptionDialog(this, new JScrollPane(area), "Receipt " + receiptNo,
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice == 1) {
                JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File(receiptNo + ".txt"));
                if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    Files.writeString(fc.getSelectedFile().toPath(), text);
                    UITheme.info(this, "Receipt saved.");
                }
            } else if (choice == 2) {
                area.print();
            }
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }

    public void refresh() {
        try {
            selectedBookingId = -1;
            balance = 0;
            lblBooking.setText("-");
            lblBill.setText("Rs. 0.00");
            lblPaid.setText("Rs. 0.00");
            lblBalance.setText("Rs. 0.00");
            txtAmount.setText("");
            bookingTable.setModel(bookingDAO.getAllBookings());
            bookingTable.getColumnModel().getColumn(0).setMaxWidth(50);
            paymentTable.setModel(paymentDAO.getAllPayments());
            paymentTable.getColumnModel().getColumn(0).setMaxWidth(50);
        } catch (Exception ex) {
            UITheme.dbError(this, ex);
        }
    }
}
