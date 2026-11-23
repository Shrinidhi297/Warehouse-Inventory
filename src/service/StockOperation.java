package service;

import java.io.IOException;
import exception.InsufficientStockException;

/** Defines the stock movement operations. */
public interface StockOperation {
    /** Increases available stock. */
    void stockIn(String productId, int quantity, String username) throws IOException;

    /** Decreases available stock if enough is on hand. */
    void stockOut(String productId, int quantity, String username) throws IOException, InsufficientStockException;
}
