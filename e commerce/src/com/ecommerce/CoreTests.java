package com.ecommerce;

import com.ecommerce.model.Product;
import com.ecommerce.model.ShoppingCart;
import com.ecommerce.model.Order;
import com.ecommerce.persistence.FileHandler;
import com.ecommerce.service.CatalogService;
import com.ecommerce.service.OrderService;

import java.util.List;

public class CoreTests {

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("          RUNNING ECOMMERCE CORE TEST SUITE          ");
        System.out.println("====================================================");

        try {
            testProductCSVSerialization();
            testShoppingCartCalculations();
            testCheckoutWorkflow();

            System.out.println("\n>>> ALL TESTS PASSED SUCCESSFULLY! <<<");
        } catch (Throwable e) {
            System.err.println("\n>>> TEST SUITE FAILED! <<<");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testProductCSVSerialization() {
        System.out.print("Testing Product CSV Serialization... ");
        Product p1 = new Product("E001", "Developer Laptop", "Sleek, fast, 16GB; AMD", 999.99, "Electronics", 5, "laptop.png");
        String csv = p1.toCSV();
        Product p2 = Product.fromCSV(csv);

        assertEqual(p1.getId(), p2.getId(), "Product ID mismatch");
        assertEqual(p1.getName(), p2.getName(), "Product Name mismatch");
        assertEqual(p1.getDescription().replace(";", ""), p2.getDescription(), "Product Description mismatch");
        assertEqual(p1.getPrice(), p2.getPrice(), "Product Price mismatch");
        assertEqual(p1.getCategory(), p2.getCategory(), "Product Category mismatch");
        assertEqual(p1.getStockQuantity(), p2.getStockQuantity(), "Product Stock mismatch");
        assertEqual(p1.getImageName(), p2.getImageName(), "Product ImageName mismatch");
        System.out.println("PASSED");
    }

    private static void testShoppingCartCalculations() {
        System.out.print("Testing Shopping Cart Calculations... ");
        Product p1 = new Product("P001", "Item A", "Desc A", 10.00, "Cat", 10, "");
        Product p2 = new Product("P002", "Item B", "Desc B", 20.00, "Cat", 5, "");

        ShoppingCart cart = new ShoppingCart();
        
        // Add items
        boolean added1 = cart.addItem(p1, 2); // 20.00
        boolean added2 = cart.addItem(p2, 1); // 20.00
        
        assertEqual(true, added1, "Failed to add Item A");
        assertEqual(true, added2, "Failed to add Item B");
        assertEqual(3, cart.getTotalCount(), "Cart total count mismatch");

        // Subtotal = 2 * 10 + 1 * 20 = 40.00
        assertEqual(40.00, cart.getSubtotal(), "Subtotal mismatch");

        // Tax = 40 * 0.08 = 3.20
        assertEqual(3.20, cart.getTaxAmount(), "Tax mismatch");

        // Discount before coupon = 0
        assertEqual(0.00, cart.getDiscountAmount(), "Discount mismatch before coupon");

        // Apply coupon SAVE10 (10% off)
        boolean couponApplied = cart.applyCoupon("SAVE10");
        assertEqual(true, couponApplied, "Failed to apply valid coupon SAVE10");
        assertEqual(4.00, cart.getDiscountAmount(), "Discount amount mismatch after SAVE10");

        // Total = 40.00 + 3.20 - 4.00 = 39.20
        assertEqual(39.20, cart.getTotal(), "Total cart calculation mismatch");
        
        // Update quantity
        boolean updated = cart.updateQuantity("P001", 5); // 50.00 subtotal for P001
        assertEqual(true, updated, "Failed to update quantity");
        assertEqual(70.00, cart.getSubtotal(), "Subtotal after update mismatch");

        // Try exceeding stock
        boolean exceeded = cart.updateQuantity("P002", 10);
        assertEqual(false, exceeded, "Should not allow quantity exceeding stock");

        System.out.println("PASSED");
    }

    private static void testCheckoutWorkflow() throws Exception {
        System.out.print("Testing Checkout workflow... ");
        FileHandler fh = new FileHandler();
        CatalogService cs = new CatalogService(fh);
        OrderService os = new OrderService(fh, cs);

        // Add a test product
        String testId = "TEST-PROD-99";
        cs.deleteProduct(testId); // Clean if exists
        Product testProduct = new Product(testId, "Test Widget", "Unit Test Product", 15.00, "Testing", 5, "");
        cs.addNewProduct(testProduct);

        ShoppingCart cart = new ShoppingCart();
        cart.addItem(testProduct, 2);

        // Record initial history size
        int initialOrderCount = os.getOrderHistory().size();

        // Perform checkout
        Order order = os.checkout(cart, "Jane Doe", "jane@example.com", "555-0199");

        assertNotNull(order, "Order should not be null after checkout");
        assertEqual("Jane Doe", order.getCustomerName(), "Customer name mismatch in order");
        assertEqual(30.00, order.getSubtotal(), "Order subtotal mismatch");
        assertEqual(30.00 * 0.08, order.getTax(), "Order tax mismatch");
        assertEqual(30.00 * 1.08, order.getTotal(), "Order total mismatch");

        // Verify stock was reduced
        Product updatedProduct = cs.getProductById(testId);
        assertEqual(3, updatedProduct.getStockQuantity(), "Stock was not reduced correctly");

        // Verify order is in history
        assertEqual(initialOrderCount + 1, os.getOrderHistory().size(), "Order history size mismatch");

        // Verify order saved to file
        FileHandler fh2 = new FileHandler();
        OrderService os2 = new OrderService(fh2, new CatalogService(fh2));
        boolean foundOrder = false;
        for (Order o : os2.getOrderHistory()) {
            if (o.getOrderId().equals(order.getOrderId())) {
                foundOrder = true;
                assertEqual(order.getCustomerName(), o.getCustomerName(), "History customer name mismatch");
                assertEqual(order.getTotal(), o.getTotal(), "History total amount mismatch");
                assertEqual(1, o.getItems().size(), "History items count mismatch");
                assertEqual("Test Widget", o.getItems().get(0).getProduct().getName(), "History item product name mismatch");
            }
        }
        assertEqual(true, foundOrder, "Order not found in file-based history");

        // Clean up test product
        cs.deleteProduct(testId);
        System.out.println("PASSED");
    }

    // Assert helpers
    private static void assertEqual(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertEqual(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 0.0001) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message + " - Object is null");
        }
    }
}
