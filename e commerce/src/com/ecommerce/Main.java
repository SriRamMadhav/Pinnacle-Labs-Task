package com.ecommerce;

import com.ecommerce.gui.MainFrame;
import com.ecommerce.persistence.FileHandler;
import com.ecommerce.service.CatalogService;
import com.ecommerce.service.OrderService;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Run Swing components on the Event Dispatch Thread (EDT) for thread safety
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system native look and feel for modern controls matching the operating system
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Could not load native look and feel. Falling back to default.");
            }

            // Enable font antialiasing on standard Swing controls
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

            // Initialize data persistence layers
            FileHandler fileHandler = new FileHandler();

            // Initialize catalog inventory and order transaction services
            CatalogService catalogService = new CatalogService(fileHandler);
            OrderService orderService = new OrderService(fileHandler, catalogService);

            // Create and launch the core Main Frame interface
            MainFrame app = new MainFrame(catalogService, orderService);
            app.setVisible(true);
        });
    }
}
