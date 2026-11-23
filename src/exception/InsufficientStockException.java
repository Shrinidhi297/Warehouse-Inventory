package exception;

/** Signals that a stock withdrawal exceeds the available quantity. */
public class InsufficientStockException extends Exception {
    /** Builds a message with the available quantity. */
    public InsufficientStockException(int available) {
        super("Only " + available + " items available in stock.");
    }
}
