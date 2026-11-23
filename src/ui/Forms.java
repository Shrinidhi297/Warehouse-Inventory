package ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Supplies the small Swing pieces shared by the inventory forms. */
final class Forms {
    /** Creates a read-only table model. */
    static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            /** Prevents edits that would bypass validation and saving. */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /** Creates a table with a single selected row. */
    static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model) {
            /** Alternates row backgrounds while preserving selection and focus. */
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component cell = super.prepareRenderer(renderer, row, column);
                if (!isCellSelected(row, column)) {
                    cell.setBackground(row % 2 == 0 ? java.awt.Color.WHITE : Theme.PALE);
                }
                return cell;
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 34));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(Theme.PALE);
        table.getTableHeader().setFont(table.getFont().deriveFont(java.awt.Font.BOLD));
        return table;
    }

    /** Creates a form with its labels beside the fields. */
    static JPanel form(String[] labels, Component... fields) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cell = new GridBagConstraints();
        cell.anchor = GridBagConstraints.LINE_START;
        cell.insets = new Insets(4, 0, 4, 12);
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setLabelFor(fields[i]);
            cell.gridy = i;
            cell.gridx = 0;
            cell.weightx = 0;
            cell.fill = GridBagConstraints.NONE;
            panel.add(label, cell);
            cell.gridx = 1;
            cell.weightx = 1;
            cell.fill = GridBagConstraints.HORIZONTAL;
            panel.add(fields[i], cell);
        }
        return panel;
    }

    /** Returns the ID from the first column of the selected row. */
    static String selectedId(JTable table) {
        int row = table.getSelectedRow();
        return row < 0 ? null : (String) table.getValueAt(row, 0);
    }

    /** Shows a friendly message after a successful change. */
    static void success(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warehouse Inventory", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Shows a validation or storage error. */
    static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warehouse Inventory", JOptionPane.ERROR_MESSAGE);
    }
}
