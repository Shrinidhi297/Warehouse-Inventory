package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import exception.InsufficientStockException;
import model.Product;
import service.ProductService;
import service.StockOperation;

/** Lets staff record a stock receipt or withdrawal. */
public class StockPanel extends JPanel {
    private final ProductService products;
    private final StockOperation stock;
    private final String username;
    private final boolean incoming;
    private final JComboBox<String> choices = new JComboBox<>();
    private final JTextField quantity = new JTextField(22);
    private final JLabel available = new JLabel(" ");
    private final JButton submit;
    private ArrayList<Product> options = new ArrayList<>();

    /** Builds the stock movement form. */
    public StockPanel(ProductService products, StockOperation stock, String username, boolean incoming) {
        super(new BorderLayout());
        this.products = products;
        this.stock = stock;
        this.username = username;
        this.incoming = incoming;
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        content.add(Forms.form(new String[] {"Product", "Available", "Quantity"},
                choices, available, quantity), BorderLayout.CENTER);
        submit = Theme.button(incoming ? "Record Stock In" : "Record Stock Out", true);
        submit.addActionListener(event -> saveMovement());
        quantity.addActionListener(event -> saveMovement());
        choices.addActionListener(event -> showAvailable());
        content.add(submit, BorderLayout.SOUTH);
        JPanel placement = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        placement.add(content);
        add(placement, BorderLayout.NORTH);
        refresh();
    }

    /** Reloads products and retains the selected product where possible. */
    public void refresh() {
        int oldIndex = choices.getSelectedIndex();
        String selectedId = oldIndex < 0 ? null : options.get(oldIndex).getId();
        options = products.list();
        DefaultComboBoxModel<String> choicesModel = new DefaultComboBoxModel<>();
        int selected = 0;
        for (int i = 0; i < options.size(); i++) {
            Product product = options.get(i);
            choicesModel.addElement(product.getId() + " - " + product.getName());
            if (product.getId().equals(selectedId)) {
                selected = i;
            }
        }
        choices.setModel(choicesModel);
        if (!options.isEmpty()) {
            choices.setSelectedIndex(selected);
        }
        submit.setEnabled(!options.isEmpty());
        showAvailable();
    }

    /** Displays the current quantity for the selected product. */
    private void showAvailable() {
        int index = choices.getSelectedIndex();
        available.setText(index < 0 ? "No products available" : options.get(index).getQuantity() + " items");
    }

    /** Validates the input, records the movement and shows the result. */
    private void saveMovement() {
        int index = choices.getSelectedIndex();
        if (index < 0) {
            Forms.error(this, "Select a product first.");
            return;
        }
        try {
            int amount = Integer.parseInt(quantity.getText().strip());
            String productId = options.get(index).getId();
            if (incoming) {
                stock.stockIn(productId, amount, username);
            } else {
                stock.stockOut(productId, amount, username);
            }
            quantity.setText("");
            refresh();
            Forms.success(this, incoming ? "Stock added." : "Stock removed.");
        } catch (NumberFormatException error) {
            Forms.error(this, "Enter a whole number for quantity.");
        } catch (InsufficientStockException error) {
            Forms.error(this, error.getMessage());
        } catch (IOException | IllegalArgumentException error) {
            Forms.error(this, error.getMessage());
        }
    }
}
