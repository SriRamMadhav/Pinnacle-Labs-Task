package com.ecommerce.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GuiStyles {
    // Colors
    public static final Color COLOR_PRIMARY = new Color(59, 130, 246);      // #3B82F6 Blue
    public static final Color COLOR_PRIMARY_HOVER = new Color(37, 99, 235); // #2563EB Darker Blue
    public static final Color COLOR_BACKGROUND = new Color(248, 250, 252);   // #F8FAFC Very Light Slate
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);      // #0F172A Dark Slate
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);  // #64748B Slate Gray
    public static final Color COLOR_BORDER = new Color(226, 232, 240);      // #E2E8F0 Light Gray Border
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);     // #10B981 Emerald Green
    public static final Color COLOR_DANGER = new Color(239, 68, 68);        // #EF4444 Red
    public static final Color COLOR_WARNING = new Color(245, 158, 11);      // #F59E0B Amber
    public static final Color COLOR_SIDEBAR_BG = new Color(17, 24, 39);     // #111827 Dark gray/black
    public static final Color COLOR_SIDEBAR_TEXT = new Color(156, 163, 175); // #9CA3AF
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(31, 41, 55); // #1F2937

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);

    // Helpers
    public static Border getPadding(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static void applyAntialiasing(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // Custom Rounded Panel Component
    public static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private Color borderColor = COLOR_BORDER;
        private boolean showBorder = true;

        public RoundedPanel(int radius) {
            super();
            this.cornerRadius = radius;
            setOpaque(false);
            setBackground(COLOR_CARD_BG);
        }

        public void setBorderColor(Color color) {
            this.borderColor = color;
            repaint();
        }

        public void setShowBorder(boolean show) {
            this.showBorder = show;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            applyAntialiasing(g);
            Graphics2D g2 = (Graphics2D) g;

            // Draw Background
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

            // Draw Border
            if (showBorder) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, cornerRadius, cornerRadius));
            }
        }
    }

    // Custom Modern Styled Button
    public static class ModernButton extends JButton {
        private boolean hovered = false;
        private boolean pressed = false;
        private final int radius = 10;
        private Color customBgColor = COLOR_PRIMARY;
        private Color customFgColor = Color.WHITE;
        private Color customHoverColor = COLOR_PRIMARY_HOVER;

        public ModernButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(FONT_BODY_BOLD);
            setForeground(customFgColor);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    pressed = false;
                    repaint();
                }
            });
        }

        public void setColors(Color bg, Color hover, Color fg) {
            this.customBgColor = bg;
            this.customHoverColor = hover;
            this.customFgColor = fg;
            setForeground(fg);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            applyAntialiasing(g);
            Graphics2D g2 = (Graphics2D) g;

            Color bg;
            if (!isEnabled()) {
                bg = COLOR_BORDER;
            } else if (pressed) {
                bg = customHoverColor.darker();
            } else if (hovered) {
                bg = customHoverColor;
            } else {
                bg = customBgColor;
            }

            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));

            // Write Text
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(isEnabled() ? getForeground() : COLOR_TEXT_MUTED);
            g2.drawString(getText(), x, y);
        }
    }
}
