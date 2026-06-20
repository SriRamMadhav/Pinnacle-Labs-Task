package com.ecommerce.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShoppingCart {
    private final Map<String, CartItem> items;
    private double discountPercentage; // e.g. 0.10 for 10%
    private String couponCode;

    public ShoppingCart() {
        this.items = new LinkedHashMap<>(); // Maintains insertion order
        this.discountPercentage = 0.0;
        this.couponCode = "";
    }

    public boolean addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) return false;
        
        String productId = product.getId();
        if (items.containsKey(productId)) {
            CartItem existingItem = items.get(productId);
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity <= product.getStockQuantity()) {
                existingItem.setQuantity(newQuantity);
                return true;
            } else {
                return false; // Insufficient stock
            }
        } else {
            if (quantity <= product.getStockQuantity()) {
                items.put(productId, new CartItem(product, quantity));
                return true;
            } else {
                return false; // Insufficient stock
            }
        }
    }

    public boolean updateQuantity(String productId, int quantity) {
        if (!items.containsKey(productId)) return false;
        
        if (quantity <= 0) {
            items.remove(productId);
            return true;
        }

        CartItem item = items.get(productId);
        if (quantity <= item.getProduct().getStockQuantity()) {
            item.setQuantity(quantity);
            return true;
        }
        return false; // Insufficient stock
    }

    public void removeItem(String productId) {
        items.remove(productId);
    }

    public void clear() {
        items.clear();
        discountPercentage = 0.0;
        couponCode = "";
    }

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotalCount() {
        int count = 0;
        for (CartItem item : items.values()) {
            count += item.getQuantity();
        }
        return count;
    }

    public double getSubtotal() {
        double subtotal = 0.0;
        for (CartItem item : items.values()) {
            subtotal += item.getSubtotal();
        }
        return subtotal;
    }

    public double getTaxRate() {
        return 0.08; // 8% sales tax
    }

    public double getTaxAmount() {
        return getSubtotal() * getTaxRate();
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public boolean applyCoupon(String code) {
        if (code == null) return false;
        String normalizedCode = code.trim().toUpperCase();
        if (normalizedCode.equals("SAVE10")) {
            this.discountPercentage = 0.10; // 10%
            this.couponCode = normalizedCode;
            return true;
        } else if (normalizedCode.equals("SAVE20")) {
            this.discountPercentage = 0.20; // 20%
            this.couponCode = normalizedCode;
            return true;
        } else if (normalizedCode.equals("WELCOME5")) {
            this.discountPercentage = 0.05; // 5%
            this.couponCode = normalizedCode;
            return true;
        }
        return false;
    }

    public double getDiscountAmount() {
        return getSubtotal() * discountPercentage;
    }

    public double getTotal() {
        double total = getSubtotal() + getTaxAmount() - getDiscountAmount();
        return Math.max(0.0, total); // Ensure no negative total
    }
}
