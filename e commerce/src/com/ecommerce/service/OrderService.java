package com.ecommerce.service;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.ShoppingCart;
import com.ecommerce.persistence.FileHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderService {
    private final FileHandler fileHandler;
    private final CatalogService catalogService;
    private final List<Order> orderHistory;

    public OrderService(FileHandler fileHandler, CatalogService catalogService) {
        this.fileHandler = fileHandler;
        this.catalogService = catalogService;
        this.orderHistory = fileHandler.loadOrders();
    }

    /**
     * Attempts to check out the shopping cart for a customer.
     * Throws an exception or returns null if stock validation fails.
     */
    public synchronized Order checkout(ShoppingCart cart, String customerName, String customerEmail, String customerPhone) throws Exception {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout an empty cart.");
        }
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required.");
        }

        // 1. Stock Validation
        for (CartItem item : cart.getItems()) {
            String pid = item.getProduct().getId();
            int qtyNeeded = item.getQuantity();
            
            // Fetch fresh product from catalog service to verify current stock status
            var freshProduct = catalogService.getProductById(pid);
            if (freshProduct == null) {
                throw new Exception("Product " + item.getProduct().getName() + " is no longer available in the catalog.");
            }
            if (freshProduct.getStockQuantity() < qtyNeeded) {
                throw new Exception("Insufficient stock for " + freshProduct.getName() + 
                                    ". Available: " + freshProduct.getStockQuantity() + ", Cart: " + qtyNeeded);
            }
        }

        // 2. Stock Deduction
        for (CartItem item : cart.getItems()) {
            boolean success = catalogService.reduceProductStock(item.getProduct().getId(), item.getQuantity());
            if (!success) {
                throw new Exception("Failed to deduct stock for " + item.getProduct().getName());
            }
        }

        // 3. Generate Order Details
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order newOrder = new Order(
            orderId,
            customerName,
            customerEmail,
            customerPhone,
            new ArrayList<>(cart.getItems()),
            cart.getSubtotal(),
            cart.getTaxAmount(),
            cart.getDiscountAmount(),
            cart.getTotal(),
            LocalDateTime.now()
        );

        // 4. Save/Persist Order
        fileHandler.saveOrder(newOrder);
        orderHistory.add(newOrder);

        // 5. Clear the Shopping Cart
        cart.clear();

        return newOrder;
    }

    public List<Order> getOrderHistory() {
        return new ArrayList<>(orderHistory);
    }
}
