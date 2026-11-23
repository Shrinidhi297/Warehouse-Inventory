package model;

/** Records a completed stock movement. */
public class Transaction {
    private final String date;
    private final String product;
    private final String type;
    private final int quantity;
    private final String username;

    /** Creates a history entry. */
    public Transaction(String date, String product, String type, int quantity, String username) {
        if (quantity <= 0 || !("IN".equals(type) || "OUT".equals(type))) {
            throw new IllegalArgumentException("Invalid stock movement.");
        }
        this.date = date;
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.username = username;
    }

    /** Returns the date and time. */
    public String getDate() { return date; }

    /** Returns the product name. */
    public String getProduct() { return product; }

    /** Returns the movement type. */
    public String getType() { return type; }

    /** Returns the movement quantity. */
    public int getQuantity() { return quantity; }

    /** Returns the username. */
    public String getUsername() { return username; }
}
