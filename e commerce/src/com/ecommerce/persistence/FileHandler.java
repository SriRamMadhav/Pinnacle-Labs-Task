package com.ecommerce.persistence;

import com.ecommerce.model.Order;
import com.ecommerce.model.Product;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private static final String DATA_DIR = "data";
    private static final String PRODUCTS_FILE = DATA_DIR + "/products.csv";
    private static final String ORDERS_FILE = DATA_DIR + "/orders.csv";

    public FileHandler() {
        createDataDirectory();
    }

    private void createDataDirectory() {
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Load products from CSV. If file doesn't exist, create it with default items.
     */
    public List<Product> loadProducts() {
        List<Product> products = new ArrayList<>();
        File file = new File(PRODUCTS_FILE);

        if (!file.exists()) {
            return initializeDefaultProducts();
        }

        try (BufferedReader br = Files.newBufferedReader(Paths.get(PRODUCTS_FILE), StandardCharsets.UTF_8)) {
            String line;
            // Skip header if it exists
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine && (line.startsWith("id;") || line.startsWith("ID;"))) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;
                
                if (line.trim().isEmpty()) continue;
                try {
                    products.add(Product.fromCSV(line));
                } catch (Exception e) {
                    System.err.println("Error parsing product line: " + line + ". Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading products file: " + e.getMessage());
        }

        return products;
    }

    /**
     * Save current products list to CSV.
     */
    public void saveProducts(List<Product> products) {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(PRODUCTS_FILE), StandardCharsets.UTF_8)) {
            bw.write("id;name;description;price;category;stockQuantity;imageName");
            bw.newLine();
            for (Product p : products) {
                bw.write(p.toCSV());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving products file: " + e.getMessage());
        }
    }

    /**
     * Load orders from CSV.
     */
    public List<Order> loadOrders() {
        List<Order> orders = new ArrayList<>();
        File file = new File(ORDERS_FILE);

        if (!file.exists()) {
            return orders; // return empty list
        }

        try (BufferedReader br = Files.newBufferedReader(Paths.get(ORDERS_FILE), StandardCharsets.UTF_8)) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine && (line.startsWith("orderId;") || line.startsWith("OrderID;"))) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;
                
                if (line.trim().isEmpty()) continue;
                try {
                    orders.add(Order.fromCSV(line));
                } catch (Exception e) {
                    System.err.println("Error parsing order line: " + line + ". Error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading orders file: " + e.getMessage());
        }

        return orders;
    }

    /**
     * Append a new order to the orders CSV.
     */
    public void saveOrder(Order order) {
        File file = new File(ORDERS_FILE);
        boolean writeHeader = !file.exists() || file.length() == 0;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ORDERS_FILE, StandardCharsets.UTF_8, true))) {
            if (writeHeader) {
                bw.write("orderId;customerName;customerEmail;customerPhone;subtotal;tax;discount;total;timestamp;serializedItems");
                bw.newLine();
            }
            bw.write(order.toCSV());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error saving order: " + e.getMessage());
        }
    }

    private List<Product> initializeDefaultProducts() {
        List<Product> defaults = new ArrayList<>();
        // Electronics
        defaults.add(new Product("E001", "Laptop Pro 15", "High-performance laptop for developers and creators with 32GB RAM, 1TB SSD.", 1299.99, "Electronics", 10, "laptop"));
        defaults.add(new Product("E002", "Smartphone X", "Latest 5G smartphone with a stunning 6.7-inch OLED display and advanced camera.", 799.49, "Electronics", 15, "smartphone"));
        defaults.add(new Product("E003", "Noise Cancelling Headphones", "Wireless over-ear headphones with active noise cancellation and 40h battery.", 199.99, "Electronics", 20, "headphones"));
        // Apparel
        defaults.add(new Product("A001", "Premium Cotton T-Shirt", "Minimalist soft organic cotton t-shirt in classic pitch black, breathable fit.", 24.99, "Apparel", 50, "tshirt"));
        defaults.add(new Product("A002", "Urban Hoodie", "Cozy streetwear pullover hoodie with fleece lining and comfortable pocket.", 49.99, "Apparel", 30, "hoodie"));
        defaults.add(new Product("A003", "Leather Sneakers", "Classic white leather sneakers for everyday smart casual style.", 89.99, "Apparel", 12, "sneakers"));
        // Home & Kitchen
        defaults.add(new Product("H001", "Smart Coffee Maker", "Programmable drip coffee maker with app control, timer, and thermal carafe.", 120.00, "Home & Kitchen", 8, "coffeemaker"));
        defaults.add(new Product("H002", "LED Desk Lamp", "Dimmable desk lamp with USB charging ports, flexible neck, and 5 color modes.", 34.50, "Home & Kitchen", 25, "lamp"));
        defaults.add(new Product("H003", "Vacuum Insulated Flask", "Stainless steel double-wall leakproof water bottle, keeps cold 24h, 1L.", 19.99, "Home & Kitchen", 40, "flask"));

        saveProducts(defaults);
        return defaults;
    }
}
