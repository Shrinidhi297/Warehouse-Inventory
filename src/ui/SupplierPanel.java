package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Supplier;
import service.SupplierService;

/** Lists suppliers and lets admins add or edit them. */
public class SupplierPanel extends JPanel {
    private final SupplierService suppliers;
    private final DefaultTableModel rows = Forms.tableModel("Supplier ID", "Name", "Phone", "City");
    private final JTable table = Forms.table(rows);

    /** Builds the supplier list and controls. */
    public SupplierPanel(SupplierService suppliers) {
        super(new BorderLayout(8, 8));
        this.suppliers = suppliers;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = Theme.button("Add", true);
        add.addActionListener(event -> showForm(null));
        JButton edit = Theme.button("Edit", false);
        edit.addActionListener(event -> {
            Supplier supplier = suppliers.find(Forms.selectedId(table));
            if (supplier == null) {
                Forms.error(this, "Select a supplier first.");
            } else {
                showForm(supplier);
            }
        });
        actions.add(add);
        actions.add(edit);
        add(actions, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    /** Reloads the supplier table. */
    public void refresh() {
        rows.setRowCount(0);
        for (Supplier supplier : suppliers.list()) {
            rows.addRow(new Object[] {supplier.getId(), supplier.getName(), supplier.getPhone(), supplier.getCity()});
        }
    }

    /** Opens a form for a new or existing supplier. */
    private void showForm(Supplier current) {
        JTextField id = new JTextField(current == null ? "" : current.getId(), 16);
        id.setEditable(current == null);
        JTextField name = new JTextField(current == null ? "" : current.getName());
        JTextField phone = new JTextField(current == null ? "" : current.getPhone());
        JTextField city = new JTextField(current == null ? "" : current.getCity());
        JPanel form = Forms.form(new String[] {"Supplier ID", "Name", "Phone", "City"}, id, name, phone, city);
        String title = current == null ? "Add Supplier" : "Edit Supplier";
        while (JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION)
                == JOptionPane.OK_OPTION) {
            try {
                Supplier supplier = new Supplier(id.getText().strip(), name.getText().strip(),
                        phone.getText().strip(), city.getText().strip());
                if (current == null) {
                    suppliers.add(supplier);
                } else {
                    suppliers.edit(supplier);
                }
                refresh();
                Forms.success(this, "Supplier saved.");
                return;
            } catch (IOException | IllegalArgumentException error) {
                Forms.error(this, error.getMessage());
            }
        }
    }
}
