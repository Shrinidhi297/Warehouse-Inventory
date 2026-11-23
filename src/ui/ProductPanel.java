package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.Product;
import model.Supplier;
import service.ProductService;
import service.SupplierService;

/** Lists and searches products, with editing controls for admins. */
public class ProductPanel extends JPanel {
    private final ProductService products;
    private final SupplierService suppliers;
    private final JTextField search = new JTextField(20);
    private final DefaultTableModel rows = Forms.tableModel(
            "Product ID", "Name", "Category", "Quantity", "Price (Rs)", "Reorder Level", "Supplier");
    private final JTable table = Forms.table(rows);

    /** Builds the product view for the given access level. */
    public ProductPanel(ProductService products, SupplierService suppliers, boolean canEdit) {
        super(new BorderLayout(8, 8));
        this.products = products;
        this.suppliers = suppliers;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Search by name or category");
        label.setLabelFor(search);
        actions.add(label);
        actions.add(search);
        search.getDocument().addDocumentListener(new DocumentListener() {
            /** Updates results when typing. */
            public void insertUpdate(DocumentEvent event) { refresh(); }
            /** Updates results when deleting. */
            public void removeUpdate(DocumentEvent event) { refresh(); }
            /** Updates results when the document changes. */
            public void changedUpdate(DocumentEvent event) { refresh(); }
        });
        if (canEdit) {
            JButton add = Theme.button("Add", true);
            add.addActionListener(event -> showForm(null));
            JButton edit = Theme.button("Edit", false);
            edit.addActionListener(event -> {
                Product product = selectedProduct();
                if (product != null) {
                    showForm(product);
                }
            });
            JButton delete = Theme.button("Delete", false);
            delete.addActionListener(event -> deleteProduct());
            actions.add(add);
            actions.add(edit);
            actions.add(delete);
        }
        add(actions, BorderLayout.NORTH);
        table.getColumnModel().getColumn(6).setPreferredWidth(180);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    /** Reloads the rows for the current search text. */
    public void refresh() {
        rows.setRowCount(0);
        for (Product product : products.search(search.getText())) {
            Supplier supplier = suppliers.find(product.getSupplierId());
            rows.addRow(new Object[] {product.getId(), product.getName(), product.getCategory(),
                    product.getQuantity(), product.getPrice().toPlainString(), product.getReorderLevel(),
                    supplier == null ? product.getSupplierId() : supplier.getName()});
        }
    }

    /** Gets the selected product or asks the admin to select a row. */
    private Product selectedProduct() {
        Product product = products.find(Forms.selectedId(table));
        if (product == null) {
            Forms.error(this, "Select a product first.");
        }
        return product;
    }

    /** Opens the form for a new or existing product. */
    private void showForm(Product current) {
        JTextField id = new JTextField(current == null ? "" : current.getId(), 16);
        id.setEditable(current == null);
        JTextField name = new JTextField(current == null ? "" : current.getName());
        JTextField category = new JTextField(current == null ? "" : current.getCategory());
        JTextField quantity = new JTextField(current == null ? "0" : String.valueOf(current.getQuantity()));
        JTextField price = new JTextField(current == null ? "" : current.getPrice().toPlainString());
        JTextField reorder = new JTextField(current == null ? "0" : String.valueOf(current.getReorderLevel()));
        JComboBox<String> supplierOptions = new JComboBox<>();
        for (Supplier supplier : suppliers.list()) {
            supplierOptions.addItem(supplier.getId() + " - " + supplier.getName());
            if (current != null && supplier.getId().equalsIgnoreCase(current.getSupplierId())) {
                supplierOptions.setSelectedIndex(supplierOptions.getItemCount() - 1);
            }
        }
        JPanel form = Forms.form(new String[] {"Product ID", "Name", "Category", "Quantity",
                "Price (Rs)", "Reorder Level", "Supplier"}, id, name, category, quantity, price, reorder, supplierOptions);
        String title = current == null ? "Add Product" : "Edit Product";
        while (JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION)
                == JOptionPane.OK_OPTION) {
            try {
                int index = supplierOptions.getSelectedIndex();
                if (index < 0) {
                    throw new IllegalArgumentException("Add a supplier before saving a product.");
                }
                String supplierId = suppliers.list().get(index).getId();
                Product product = new Product(id.getText().strip(), name.getText().strip(), category.getText().strip(),
                        Integer.parseInt(quantity.getText().strip()), new BigDecimal(price.getText().strip()),
                        Integer.parseInt(reorder.getText().strip()), supplierId);
                if (current == null) {
                    products.add(product);
                } else {
                    products.edit(product);
                }
                refresh();
                Forms.success(this, "Product saved.");
                return;
            } catch (NumberFormatException error) {
                Forms.error(this, "Enter a valid price and whole numbers for quantity and reorder level.");
            } catch (IOException | IllegalArgumentException error) {
                Forms.error(this, error.getMessage());
            }
        }
    }

    /** Confirms and deletes the selected product. */
    private void deleteProduct() {
        Product product = selectedProduct();
        if (product == null || JOptionPane.showConfirmDialog(this, "Delete " + product.getName() + "?",
                "Delete Product", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            products.delete(product.getId());
            refresh();
            Forms.success(this, "Product deleted.");
        } catch (IOException error) {
            Forms.error(this, error.getMessage());
        }
    }
}
