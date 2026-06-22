package com.weather;

import com.formdev.flatlaf.FlatDarkLaf;
import com.weather.ui.WeatherFrame;

import javax.swing.*;

/**
 * Entry point of the Weather Forecast Application.
 */
public class Main {
    public static void main(String[] args) {
        // Run UI creation on the Event Dispatch Thread (EDT) for Swing safety
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize FlatLaf Dark Theme
                if (!FlatDarkLaf.setup()) {
                    System.err.println("Failed to setup FlatLaf Look and Feel. Falling back to default.");
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            } catch (Exception e) {
                System.err.println("Error setting up Look and Feel: " + e.getMessage());
            }

            // Launch the main window
            WeatherFrame frame = new WeatherFrame();
            frame.setVisible(true);
        });
    }
}
