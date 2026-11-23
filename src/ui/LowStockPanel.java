package ui;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Product;
import service.ProductService;

/** Displays products at or below their reorder levels. */
public class LowStockPanel extends JPanel {
    private final ProductService products;
    private final DefaultTableModel rows = Forms.tableModel("Product ID", "Name", "Category", "Quantity", "Reorder Level");
    private final JLabel empty = new JLabel("All products are above their reorder levels.");

    /** Builds the read-only table. */
    public LowStockPanel(ProductService products) {
        super(new BorderLayout(8, 8));
        this.products = products;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(empty, BorderLayout.NORTH);
        JTable table = Forms.table(rows);
        
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    /** Reloads the table and shows a message when no rows match. */
    public void refresh() {
        rows.setRowCount(0);
        for (Product item : products.lowStock()) {
            rows.addRow(new Object[] {item.getId(), item.getName(), item.getCategory(), item.getQuantity(), item.getReorderLevel()});
        }
        empty.setVisible(rows.getRowCount() == 0);
    }
}
