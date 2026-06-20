import gui.MainFrame;
import services.LibraryService;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Apply System native UI styling
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set system look and feel: " + e.getMessage());
        }

        // Start UI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LibraryService libraryService = new LibraryService();
            MainFrame mainFrame = new MainFrame(libraryService);
            mainFrame.setVisible(true);
        });
    }
}
