package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.persistence.FileHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CatalogService {
    private final FileHandler fileHandler;
    private final List<Product> products;

    public CatalogService(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        this.products = fileHandler.loadProducts();
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }

    public List<Product> searchAndFilter(String query, String category) {
        return products.stream()
                .filter(p -> {
                    if (category == null || category.isEmpty() || category.equalsIgnoreCase("All Categories")) {
                        return true;
                    }
                    return p.getCategory().equalsIgnoreCase(category);
                })
                .filter(p -> {
                    if (query == null || query.trim().isEmpty()) {
                        return true;
                    }
                    String q = query.toLowerCase().trim();
                    return p.getName().toLowerCase().contains(q) || 
                           p.getDescription().toLowerCase().contains(q);
                })
                .collect(Collectors.toList());
    }

    public Set<String> getCategories() {
        Set<String> categories = new TreeSet<>();
        categories.add("All Categories");
        for (Product p : products) {
            categories.add(p.getCategory());
        }
        return categories;
    }

    public boolean reduceProductStock(String productId, int quantity) {
        for (Product p : products) {
            if (p.getId().equals(productId)) {
                boolean success = p.reduceStock(quantity);
                if (success) {
                    fileHandler.saveProducts(products); // persist state
                }
                return success;
            }
        }
        return false;
    }

    public void restockProduct(String productId, int quantity) {
        for (Product p : products) {
            if (p.getId().equals(productId)) {
                p.increaseStock(quantity);
                fileHandler.saveProducts(products); // persist state
                return;
            }
        }
    }

    public boolean addNewProduct(Product product) {
        if (product == null || product.getId() == null || product.getId().trim().isEmpty()) {
            return false;
        }
        // Check if ID already exists
        for (Product p : products) {
            if (p.getId().equalsIgnoreCase(product.getId())) {
                return false;
            }
        }
        products.add(product);
        fileHandler.saveProducts(products);
        return true;
    }

    public boolean updateProduct(Product updatedProduct) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(updatedProduct.getId())) {
                products.set(i, updatedProduct);
                fileHandler.saveProducts(products);
                return true;
            }
        }
        return false;
    }

    public boolean deleteProduct(String productId) {
        boolean removed = products.removeIf(p -> p.getId().equals(productId));
        if (removed) {
            fileHandler.saveProducts(products);
        }
        return removed;
    }

    public Product getProductById(String productId) {
        for (Product p : products) {
            if (p.getId().equals(productId)) {
                return p;
            }
        }
        return null;
    }
}
