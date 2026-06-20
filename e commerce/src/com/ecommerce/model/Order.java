package com.ecommerce.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private List<CartItem> items;
    private double subtotal;
    private double tax;
    private double discount;
    private double total;
    private LocalDateTime timestamp;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Order(String orderId, String customerName, String customerEmail, String customerPhone, 
                 List<CartItem> items, double subtotal, double tax, double discount, double total, LocalDateTime timestamp) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.items = new ArrayList<>(items);
        this.subtotal = subtotal;
        this.tax = tax;
        this.discount = discount;
        this.total = total;
        this.timestamp = timestamp;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public List<CartItem> getItems() { return items; }
    public double getSubtotal() { return subtotal; }
    public double getTax() { return tax; }
    public double getDiscount() { return discount; }
    public double getTotal() { return total; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedTimestamp() {
        return timestamp.format(DISPLAY_FORMATTER);
    }

    // CSV format: orderId;customerName;customerEmail;customerPhone;subtotal;tax;discount;total;timestamp;serializedItems
    // serializedItems format: id:name:price:qty|id2:name2:price2:qty2
    public String toCSV() {
        List<String> itemTokens = new ArrayList<>();
        for (CartItem item : items) {
            Product p = item.getProduct();
            // Escape any special characters
            String pName = p.getName().replace(":", "").replace("|", "").replace(";", "");
            itemTokens.add(p.getId() + ":" + pName + ":" + p.getPrice() + ":" + item.getQuantity());
        }
        String serializedItems = String.join("|", itemTokens);

        return String.join(";",
            orderId,
            customerName.replace(";", ""),
            customerEmail.replace(";", ""),
            customerPhone.replace(";", ""),
            String.valueOf(subtotal),
            String.valueOf(tax),
            String.valueOf(discount),
            String.valueOf(total),
            timestamp.format(FORMATTER),
            serializedItems
        );
    }

    public static Order fromCSV(String csvLine) {
        String[] parts = csvLine.split(";");
        if (parts.length < 10) {
            throw new IllegalArgumentException("Invalid CSV line for Order: " + csvLine);
        }

        String orderId = parts[0];
        String customerName = parts[1];
        String customerEmail = parts[2];
        String customerPhone = parts[3];
        double subtotal = Double.parseDouble(parts[4]);
        double tax = Double.parseDouble(parts[5]);
        double discount = Double.parseDouble(parts[6]);
        double total = Double.parseDouble(parts[7]);
        LocalDateTime timestamp = LocalDateTime.parse(parts[8], FORMATTER);
        
        List<CartItem> items = new ArrayList<>();
        String serializedItems = parts[9];
        if (!serializedItems.trim().isEmpty()) {
            String[] itemTokens = serializedItems.split("\\|");
            for (String token : itemTokens) {
                String[] itemParts = token.split(":");
                if (itemParts.length == 4) {
                    String pid = itemParts[0];
                    String name = itemParts[1];
                    double price = Double.parseDouble(itemParts[2]);
                    int qty = Integer.parseInt(itemParts[3]);
                    
                    // Create dummy product for history display since it captures historical prices
                    Product dummyProduct = new Product(pid, name, "", price, "", 0, "");
                    items.add(new CartItem(dummyProduct, qty));
                }
            }
        }

        return new Order(orderId, customerName, customerEmail, customerPhone, items, subtotal, tax, discount, total, timestamp);
    }
}
