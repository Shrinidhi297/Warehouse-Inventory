package service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import model.Product;
import model.Transaction;
import exception.InsufficientStockException;
import storage.FileHandler;

/** Changes quantities and records stock movements. */
public class StockService implements StockOperation {
    private static final String FILE = "transactions.csv";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ProductService products;
    private final FileHandler files;
    private final ArrayList<Transaction> history = new ArrayList<>();

    /** Loads the stock movement history. */
    public StockService(ProductService products, FileHandler files) throws IOException {
        this.products = products;
        this.files = files;
        if (files.exists(FILE)) {
            for (String[] row : files.read(FILE)) {
                history.add(new Transaction(row[0], row[1], row[2], Integer.parseInt(row[3]), row[4]));
            }
        }
    }

    /** Adds stock without exceeding the integer quantity limit. */
    @Override
    public void stockIn(String productId, int quantity, String username) throws IOException {
        Product product = requireProduct(productId, quantity);
        if (quantity > Integer.MAX_VALUE - product.getQuantity()) {
            throw new IllegalArgumentException("The new stock quantity is too large.");
        }
        record(product, product.getQuantity() + quantity, "IN", quantity, username);
    }

    /** Removes stock or reports the exact amount available. */
    @Override
    public void stockOut(String productId, int quantity, String username)
            throws IOException, InsufficientStockException {
        Product product = requireProduct(productId, quantity);
        if (quantity > product.getQuantity()) {
            throw new InsufficientStockException(product.getQuantity());
        }
        record(product, product.getQuantity() - quantity, "OUT", quantity, username);
    }

    /** Returns recorded movements in their original order. */
    public ArrayList<Transaction> history() {
        return new ArrayList<>(history);
    }

    /** Checks the quantity and finds the product. */
    private Product requireProduct(String id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Enter a quantity greater than zero.");
        }
        Product product = products.find(id);
        if (product == null) {
            throw new IllegalArgumentException("Select a product.");
        }
        return product;
    }

    /** Saves both changes and restores the old quantity if the history cannot be written. */
    private void record(Product product, int newQuantity, String type, int quantity, String username)
            throws IOException {
        Product updated = new Product(product.getId(), product.getName(), product.getCategory(),
                newQuantity, product.getPrice(), product.getReorderLevel(), product.getSupplierId());
        Transaction movement = new Transaction(LocalDateTime.now().format(DATE),
                product.getName(), type, quantity, username);
        ArrayList<String[]> rows = new ArrayList<>();
        for (Transaction entry : history) {
            rows.add(toRow(entry));
        }
        rows.add(toRow(movement));
        products.edit(updated);
        try {
            files.write(FILE, rows);
        } catch (IOException | IllegalArgumentException failure) {
            try {
                products.edit(product);
            } catch (IOException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
                throw new IOException("Could not save history or restore the previous stock quantity.", failure);
            }
            throw failure;
        }
        history.add(movement);
    }

    /** Converts one movement to a CSV row. */
    private String[] toRow(Transaction entry) {
        return new String[] {entry.getDate(), entry.getProduct(), entry.getType(),
                String.valueOf(entry.getQuantity()), entry.getUsername()};
    }
}
