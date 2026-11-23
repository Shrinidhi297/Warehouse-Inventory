package ui;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Transaction;
import service.StockService;

/** Displays completed stock movements. */
public class HistoryPanel extends JPanel {
    private final StockService stock;
    private final DefaultTableModel rows = Forms.tableModel("Date", "Product", "Type", "Quantity", "User");
    private final JLabel empty = new JLabel("No stock movements yet.");

    /** Builds the read-only table. */
    public HistoryPanel(StockService stock) {
        super(new BorderLayout(8, 8));
        this.stock = stock;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(empty, BorderLayout.NORTH);
        JTable table = Forms.table(rows);
        table.getColumnModel().getColumn(0).setPreferredWidth(145);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    /** Reloads the table and shows a message when no rows match. */
    public void refresh() {
        rows.setRowCount(0);
        for (Transaction item : stock.history()) {
            rows.addRow(new Object[] {item.getDate(), item.getProduct(), item.getType(), item.getQuantity(), item.getUsername()});
        }
        empty.setVisible(rows.getRowCount() == 0);
    }
}
