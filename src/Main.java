import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import ui.Theme;
import java.io.IOException;
import service.LoginService;
import service.ProductService;
import service.StockService;
import service.SupplierService;
import storage.FileHandler;
import ui.LoginFrame;

/** Starts the warehouse application on Swing's event thread. */
public class Main {
    /** Loads the local data and opens the login screen. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            try {
                FileHandler files = new FileHandler();
                SupplierService suppliers = new SupplierService(files);
                ProductService products = new ProductService(files);
                LoginService login = new LoginService(files);
                StockService stock = new StockService(products, files);
                new LoginFrame(login, products, suppliers, stock).setVisible(true);
            } catch (IOException | RuntimeException error) {
                JOptionPane.showMessageDialog(null, "Could not load the inventory data. " + error.getMessage(),
                        "Startup error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
