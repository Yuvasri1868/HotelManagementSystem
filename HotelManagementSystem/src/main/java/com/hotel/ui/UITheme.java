package com.hotel.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;

/** Colors, fonts and small UI helper methods so every screen looks the same. */
public class UITheme {
    public static final Color PRIMARY = new Color(23, 42, 69);      // dark navy
    public static final Color PRIMARY_LIGHT = new Color(36, 62, 99);
    public static final Color ACCENT = new Color(212, 160, 23);     // gold
    public static final Color BG = new Color(242, 245, 249);
    public static final Color WHITE = Color.WHITE;
    public static final Color SUCCESS = new Color(39, 174, 96);
    public static final Color DANGER = new Color(192, 57, 43);
    public static final Color INFO = new Color(41, 128, 185);
    public static final Color WARN = new Color(230, 126, 34);
    public static final Color GRAY = new Color(127, 140, 141);

    public static final Font FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 24);

    /** Colored flat button. */
    public static JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(BOLD);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(9, 16, 9, 16));
        return b;
    }

    public static JTextField field() {
        JTextField f = new JTextField();
        f.setFont(FONT);
        f.setPreferredSize(new Dimension(190, 32));
        return f;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BOLD);
        l.setForeground(PRIMARY);
        return l;
    }

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> c = new JComboBox<>(items);
        c.setFont(FONT);
        c.setPreferredSize(new Dimension(190, 32));
        return c;
    }

    /** Page title bar shown at the top of each module. */
    public static JPanel titleBar(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(15, 20, 5, 20));
        JLabel l = new JLabel(title);
        l.setFont(TITLE);
        l.setForeground(PRIMARY);
        p.add(l, BorderLayout.WEST);
        return p;
    }

    /** White card-style form panel. */
    public static JPanel formPanel(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(210, 215, 222)), title,
                        javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, BOLD, PRIMARY),
                new EmptyBorder(10, 12, 10, 12)));
        return p;
    }

    /** Adds "Label : component" as one row to a GridBagLayout panel. */
    public static void addRow(JPanel form, int row, String text, JComponent comp) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 4, 5, 4);
        g.gridx = 0; g.gridy = row; g.anchor = GridBagConstraints.WEST;
        form.add(label(text), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        form.add(comp, g);
    }

    /** Adds a full-width component (e.g. a button row) to a GridBagLayout form. */
    public static void addWide(JPanel form, int row, JComponent comp) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 4, 4, 4);
        g.gridx = 0; g.gridy = row; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        form.add(comp, g);
    }

    /** Styled JTable inside a scroll pane. */
    public static JTable table(TableModel model) {
        JTable t = new JTable(model);
        t.setFont(FONT);
        t.setRowHeight(28);
        t.setSelectionBackground(new Color(214, 234, 248));
        t.setSelectionForeground(Color.BLACK);
        t.setGridColor(new Color(228, 232, 238));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setAutoCreateRowSorter(true);
        JTableHeader h = t.getTableHeader();
        h.setFont(BOLD);
        h.setBackground(PRIMARY);
        h.setForeground(Color.WHITE);
        h.setPreferredSize(new Dimension(100, 32));
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setBorder(new EmptyBorder(0, 6, 0, 6));
        t.setDefaultRenderer(Object.class, r);
        return t;
    }

    public static JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.getViewport().setBackground(WHITE);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 222)));
        return sp;
    }

    /** Value of the selected row's column in the table (null if no row is selected). */
    public static Object selected(JTable t, int column) {
        int row = t.getSelectedRow();
        if (row < 0) return null;
        return t.getModel().getValueAt(t.convertRowIndexToModel(row), column);
    }

    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Hotel Management", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Please confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Shows a friendly message for common database errors. */
    public static void dbError(Component parent, Exception e) {
        String m = e.getMessage() == null ? e.toString() : e.getMessage();
        if (m.contains("Duplicate entry")) m = "This value already exists (duplicate entry).";
        else if (m.contains("foreign key constraint"))
            m = "This record is used by bookings/payments, so it cannot be deleted.";
        error(parent, m);
        e.printStackTrace();
    }

    /** Exports a table to a CSV file chosen by the user. */
    public static void exportCsv(Component parent, TableModel m) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("report.csv"));
        if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter w = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
            for (int c = 0; c < m.getColumnCount(); c++) w.print((c > 0 ? "," : "") + m.getColumnName(c));
            w.println();
            for (int r = 0; r < m.getRowCount(); r++) {
                for (int c = 0; c < m.getColumnCount(); c++) {
                    Object v = m.getValueAt(r, c);
                    String s = v == null ? "" : v.toString().replace("\"", "\"\"");
                    w.print((c > 0 ? "," : "") + "\"" + s + "\"");
                }
                w.println();
            }
            info(parent, "Exported to " + fc.getSelectedFile().getAbsolutePath());
        } catch (Exception e) {
            error(parent, "Could not export: " + e.getMessage());
        }
    }
}
