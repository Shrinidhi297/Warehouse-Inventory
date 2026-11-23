package model;
import java.math.BigDecimal;

/** Stores product details. */
public class Product {
    private String id;
    private String name;
    private String category;
    private int quantity;
    private BigDecimal price;
    private int reorderLevel;
    private String supplierId;

    /** Creates a validated product. */
    public Product(String id, String name, String category, int quantity, BigDecimal price, int reorderLevel, String supplierId) {
        setId(id);
        setName(name);
        setCategory(category);
        setQuantity(quantity);
        setPrice(price);
        setReorderLevel(reorderLevel);
        setSupplierId(supplierId);
    }

    /** Returns the id. */
    public String getId() {
        return id;
    }

    /** Sets the id. */
    public void setId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid id.");
        }
        this.id = id;
    }

    /** Returns the name. */
    public String getName() {
        return name;
    }

    /** Sets the name. */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid name.");
        }
        this.name = name;
    }

    /** Returns the category. */
    public String getCategory() {
        return category;
    }

    /** Sets the category. */
    public void setCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Invalid category.");
        }
        this.category = category;
    }

    /** Returns the quantity. */
    public int getQuantity() {
        return quantity;
    }

    /** Sets the quantity. */
    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Invalid quantity.");
        }
        this.quantity = quantity;
    }

    /** Returns the price. */
    public BigDecimal getPrice() {
        return price;
    }

    /** Sets the price. */
    public void setPrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Invalid price.");
        }
        this.price = price;
    }

    /** Returns the reorderLevel. */
    public int getReorderLevel() {
        return reorderLevel;
    }

    /** Sets the reorderLevel. */
    public void setReorderLevel(int reorderLevel) {
        if (reorderLevel < 0) {
            throw new IllegalArgumentException("Invalid reorder level.");
        }
        this.reorderLevel = reorderLevel;
    }

    /** Returns the supplierId. */
    public String getSupplierId() {
        return supplierId;
    }

    /** Sets the supplierId. */
    public void setSupplierId(String supplierId) {
        if (supplierId == null || supplierId.isBlank()) {
            throw new IllegalArgumentException("Invalid supplier id.");
        }
        this.supplierId = supplierId;
    }
    /** Returns whether the product needs reordering. */
    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }
}
