package service;

import java.io.IOException;
import java.util.ArrayList;
import model.Supplier;
import storage.FileHandler;

/** Loads, validates and saves the supplier list. */
public class SupplierService {
    private static final String FILE = "suppliers.csv";
    private final FileHandler files;
    private ArrayList<Supplier> items = new ArrayList<>();

    /** Loads saved data or creates the demo data. */
    public SupplierService(FileHandler files) throws IOException {
        this.files = files;
        if (!files.exists(FILE)) {
            files.write(FILE, new ArrayList<>(java.util.List.of(
                    new String[] {"S1", "Coastal Grains", "9845012345", "Mangaluru"},
                    new String[] {"S2", "Metro Foods", "9820012345", "Mumbai"},
                    new String[] {"S3", "Garden City Traders", "9886012345", "Bengaluru"},
                    new String[] {"S4", "Marina Stationery", "9840012345", "Chennai"},
                    new String[] {"S5", "Capital Wholesale", "9810012345", "Delhi"})));
        }
        for (String[] row : files.read(FILE)) {
            items.add(new Supplier(row[0], row[1], row[2], row[3]));
        }
    }

    /** Returns the list in display order. */
    public ArrayList<Supplier> list() {
        return new ArrayList<>(items);
    }

    /** Finds an item by its ID. */
    public Supplier find(String id) {
        for (Supplier item : items) {
            if (item.getId().equalsIgnoreCase(id)) {
                return item;
            }
        }
        return null;
    }

    /** Adds a new item with a unique ID. */
    public void add(Supplier item) throws IOException {
        if (find(item.getId()) != null) {
            throw new IllegalArgumentException("This ID already exists.");
        }
        ArrayList<Supplier> updated = list();
        updated.add(item);
        save(updated);
    }

    /** Replaces the item with the same ID. */
    public void edit(Supplier item) throws IOException {
        ArrayList<Supplier> updated = list();
        int index = updated.indexOf(find(item.getId()));
        if (index < 0) {
            throw new IllegalArgumentException("The item no longer exists.");
        }
        updated.set(index, item);
        save(updated);
    }

    /** Writes the new list before publishing it in memory. */
    private void save(ArrayList<Supplier> updated) throws IOException {
        ArrayList<String[]> rows = new ArrayList<>();
        for (Supplier item : updated) {
            rows.add(new String[] {item.getId(), item.getName(), item.getPhone(), item.getCity()});
        }
        files.write(FILE, rows);
        items = updated;
    }
}
