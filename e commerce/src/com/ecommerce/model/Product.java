package com.ecommerce.model;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String category;
    private int stockQuantity;
    private String imageName;

    public Product(String id, String name, String description, double price, String category, int stockQuantity, String imageName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.imageName = imageName;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    // Business Logic
    public boolean isAvailable() {
        return stockQuantity > 0;
    }

    public boolean reduceStock(int quantity) {
        if (quantity <= stockQuantity) {
            stockQuantity -= quantity;
            return true;
        }
        return false;
    }

    public void increaseStock(int quantity) {
        stockQuantity += quantity;
    }

    // CSV Serialization helpers (using semicolon to avoid comma conflicts in description)
    public String toCSV() {
        return String.join(";", 
            id, 
            name.replace(";", ""), 
            description.replace(";", ""), 
            String.valueOf(price), 
            category.replace(";", ""), 
            String.valueOf(stockQuantity), 
            imageName
        );
    }

    public static Product fromCSV(String csvLine) {
        String[] parts = csvLine.split(";");
        if (parts.length < 7) {
            // Fallback default image name if missing
            String img = parts.length > 5 ? parts[5] : "placeholder";
            return new Product(parts[0], parts[1], parts[2], Double.parseDouble(parts[3]), parts[4], Integer.parseInt(parts[5]), img);
        }
        return new Product(
            parts[0], 
            parts[1], 
            parts[2], 
            Double.parseDouble(parts[3]), 
            parts[4], 
            Integer.parseInt(parts[5]), 
            parts[6]
        );
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stockQuantity +
                '}';
    }
}
