package gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Theme {
    // Colors (Indian Tricolor-Inspired Palette)
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);      // Slate 900 (Deep Indigo/Navy)
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);   // Slate 800
    public static final Color SIDEBAR_ACTIVE = new Color(2, 6, 23);    // Slate 950 (Near Black)
    
    public static final Color CONTENT_BG = new Color(248, 250, 252);   // Slate 50
    public static final Color CARD_BG = new Color(255, 255, 255);      // White
    
    public static final Color TEXT_MAIN = new Color(15, 23, 42);       // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);   // Slate 500
    public static final Color TEXT_LIGHT = new Color(241, 245, 249);   // Slate 100
    
    public static final Color PRIMARY = new Color(249, 115, 22);       // Saffron Orange 500
    public static final Color PRIMARY_HOVER = new Color(234, 88, 12);  // Saffron Orange 600
    
    public static final Color SUCCESS = new Color(22, 163, 74) ;       // Ashoka Green 600
    public static final Color DANGER = new Color(220, 38, 38);         // Red 600
    public static final Color WARNING = new Color(217, 119, 6);        // Amber 600
    
    public static final Color BORDER = new Color(226, 232, 240);        // Slate 200

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_CARD_VAL = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_CARD_LBL = new Font("Segoe UI", Font.BOLD, 12);

    // Common Borders
    public static final Border BORDER_CARD = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
    );
    
    public static final Border BORDER_INPUT = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
    );

    // Style Helpers
    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    if (bg.equals(PRIMARY)) {
                        g2.setColor(PRIMARY_HOVER);
                    } else if (bg.equals(SIDEBAR_BG)) {
                        g2.setColor(SIDEBAR_HOVER);
                    } else {
                        g2.setColor(bg.darker());
                    }
                } else {
                    g2.setColor(bg);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_MAIN);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(219, 234, 254)); // Soft blue selection
        table.setSelectionForeground(TEXT_MAIN);
        table.setShowVerticalLines(false); // Modern flat look (horizontal gridlines only)

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADER);
        header.setBackground(SIDEBAR_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                super.getTableCellRendererComponent(t, val, isSel, hasFoc, row, col);
                setBackground(SIDEBAR_BG);
                setForeground(Color.WHITE);
                setFont(FONT_HEADER);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)
                ));
                return this;
            }
        });

        // Alignment & padding cell renderer
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                super.getTableCellRendererComponent(t, val, isSel, hasFoc, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // Horizontal padding
                
                // Color status column dynamically
                if (val != null) {
                    String valStr = val.toString();
                    if ("ISSUED".equals(valStr)) {
                        setForeground(PRIMARY);
                        setFont(FONT_BOLD);
                    } else if ("RETURNED".equals(valStr)) {
                        setForeground(SUCCESS);
                        setFont(FONT_BOLD);
                    } else if ("OVERDUE".equals(valStr)) {
                        setForeground(DANGER);
                        setFont(FONT_BOLD);
                    } else {
                        setForeground(TEXT_MAIN);
                        setFont(FONT_BODY);
                    }
                }
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, cellRenderer);
        table.setDefaultRenderer(Integer.class, cellRenderer);
    }
}
