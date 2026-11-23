package service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.ArrayList;
import model.Product;
import storage.FileHandler;

/** Loads, validates and saves the product list. */
public class ProductService {
    private static final String FILE = "products.csv";
    private final FileHandler files;
    private ArrayList<Product> items = new ArrayList<>();

    /** Loads saved data or creates the demo data. */
    public ProductService(FileHandler files) throws IOException {
        this.files = files;
        if (!files.exists(FILE)) {
            files.write(FILE, new ArrayList<>(java.util.List.of(
                    new String[] {"P1", "Rice", "Groceries", "20", "65.00", "10", "S1"},
                    new String[] {"P2", "Wheat Flour", "Groceries", "8", "48.00", "8", "S2"},
                    new String[] {"P3", "Soap", "Personal Care", "30", "35.00", "10", "S3"},
                    new String[] {"P4", "Notebooks", "Stationery", "5", "45.00", "10", "S4"},
                    new String[] {"P5", "Cooking Oil", "Groceries", "16", "145.00", "6", "S5"})));
        }
        for (String[] row : files.read(FILE)) {
            items.add(new Product(row[0], row[1], row[2], Integer.parseInt(row[3]),
                    new BigDecimal(row[4]), Integer.parseInt(row[5]), row[6]));
        }
    }

    /** Returns the list in display order. */
    public ArrayList<Product> list() {
        return new ArrayList<>(items);
    }

    /** Finds an item by its ID. */
    public Product find(String id) {
        for (Product item : items) {
            if (item.getId().equalsIgnoreCase(id)) {
                return item;
            }
        }
        return null;
    }

    /** Adds a new item with a unique ID. */
    public void add(Product item) throws IOException {
        if (find(item.getId()) != null) {
            throw new IllegalArgumentException("This ID already exists.");
        }
        ArrayList<Product> updated = list();
        updated.add(item);
        save(updated);
    }

    /** Replaces the item with the same ID. */
    public void edit(Product item) throws IOException {
        ArrayList<Product> updated = list();
        int index = updated.indexOf(find(item.getId()));
        if (index < 0) {
            throw new IllegalArgumentException("The item no longer exists.");
        }
        updated.set(index, item);
        save(updated);
    }

    /** Removes a product from the inventory. */
    public void delete(String id) throws IOException {
        ArrayList<Product> updated = list();
        updated.remove(find(id));
        save(updated);
    }

    /** Matches a partial name or category without regard to case. */
    public ArrayList<Product> search(String query) {
        String term = query.strip().toLowerCase(Locale.ROOT);
        ArrayList<Product> matches = new ArrayList<>();
        for (Product item : items) {
            if (item.getName().toLowerCase(Locale.ROOT).contains(term)
                    || item.getCategory().toLowerCase(Locale.ROOT).contains(term)) {
                matches.add(item);
            }
        }
        return matches;
    }

    /** Lists products at or below their reorder level. */
    public ArrayList<Product> lowStock() {
        ArrayList<Product> matches = new ArrayList<>();
        for (Product item : items) {
            if (item.isLowStock()) {
                matches.add(item);
            }
        }
        return matches;
    }

    /** Writes the new list before publishing it in memory. */
    private void save(ArrayList<Product> updated) throws IOException {
        ArrayList<String[]> rows = new ArrayList<>();
        for (Product item : updated) {
            rows.add(new String[] {item.getId(), item.getName(), item.getCategory(), String.valueOf(item.getQuantity()),
                    item.getPrice().toPlainString(), String.valueOf(item.getReorderLevel()), item.getSupplierId()});
        }
        files.write(FILE, rows);
        items = updated;
    }
}
