package com.weather.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * A Swing JComponent that draws resolution-independent, vector-based weather icons
 * using Java 2D Graphics.
 */
public class WeatherIcon extends JComponent {
    private String iconCode = "01d"; // Default: Clear Sun

    public WeatherIcon(int preferredSize) {
        setPreferredSize(new Dimension(preferredSize, preferredSize));
        setMinimumSize(new Dimension(preferredSize, preferredSize));
    }

    public void setIconCode(String iconCode) {
        this.iconCode = iconCode != null ? iconCode.trim().toLowerCase() : "01d";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h);
        int pad = size / 10;
        int d = size - 2 * pad;
        int cx = w / 2;
        int cy = h / 2;

        // Draw based on iconCode prefix (OpenWeatherMap codes: 01d, 02d, 03d, 04d, 09d, 10d, 11d, 13d, 50d, etc.)
        String prefix = iconCode.length() >= 2 ? iconCode.substring(0, 2) : "01";
        boolean isNight = iconCode.endsWith("n");

        switch (prefix) {
            case "01": // Clear Sky
                if (isNight) {
                    drawMoon(g2, cx, cy, d);
                } else {
                    drawSun(g2, cx, cy, d);
                }
                break;
            case "02": // Few Clouds
                if (isNight) {
                    drawMoon(g2, cx - d / 5, cy - d / 5, d * 4 / 5);
                } else {
                    drawSun(g2, cx - d / 5, cy - d / 5, d * 4 / 5);
                }
                drawCloud(g2, cx + d / 10, cy + d / 10, d * 3 / 4, false);
                break;
            case "03": // Scattered Clouds
            case "04": // Broken/Overcast Clouds
                drawCloud(g2, cx - d / 10, cy - d / 10, d * 4 / 5, true);
                drawCloud(g2, cx + d / 10, cy + d / 10, d * 3 / 4, false);
                break;
            case "09": // Shower Rain
            case "10": // Rain
                drawCloud(g2, cx, cy - d / 10, d * 4 / 5, false);
                drawRain(g2, cx, cy + d / 5, d * 4 / 5);
                break;
            case "11": // Thunderstorm
                drawCloud(g2, cx, cy - d / 10, d * 4 / 5, true);
                drawLightning(g2, cx, cy + d / 4, d * 3 / 5);
                break;
            case "13": // Snow
                drawCloud(g2, cx, cy - d / 10, d * 4 / 5, false);
                drawSnow(g2, cx, cy + d / 5, d * 3 / 5);
                break;
            case "50": // Mist / Fog
                drawMist(g2, cx, cy, d);
                break;
            default:
                drawSun(g2, cx, cy, d);
                break;
        }

        g2.dispose();
    }

    private void drawSun(Graphics2D g2, int cx, int cy, int size) {
        int r = size / 4;
        
        // Sun glow / rays
        g2.setColor(new Color(253, 184, 19, 60));
        g2.fillOval(cx - r - size / 8, cy - r - size / 8, 2 * r + size / 4, 2 * r + size / 4);

        // Core Sun
        g2.setColor(new Color(255, 170, 0));
        g2.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        
        // Draw rays
        g2.setColor(new Color(253, 184, 19));
        g2.setStroke(new BasicStroke(size / 20.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        int rayLength = size / 2;
        int rayStart = r + size / 12;
        
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            int x1 = (int) (cx + rayStart * cos);
            int y1 = (int) (cy + rayStart * sin);
            int x2 = (int) (cx + rayLength * cos);
            int y2 = (int) (cy + rayLength * sin);
            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawMoon(Graphics2D g2, int cx, int cy, int size) {
        int r = size / 3;
        Area moon = new Area(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
        // Cut out a circle to create crescent
        Area cut = new Area(new Ellipse2D.Double(cx - r + r / 2, cy - r - r / 5, 2 * r, 2 * r));
        moon.subtract(cut);

        g2.setColor(new Color(240, 240, 220));
        g2.fill(moon);

        // Soft outer glow
        g2.setStroke(new BasicStroke(2.0f));
        g2.setColor(new Color(240, 240, 220, 100));
        g2.draw(moon);
    }

    private void drawCloud(Graphics2D g2, int cx, int cy, int size, boolean dark) {
        int w = size;
        int h = size * 3 / 5;
        int x = cx - w / 2;
        int y = cy - h / 2;

        // Path of a fluffy cloud
        Path2D.Double cloud = new Path2D.Double();
        double rw = w / 4.0;
        double rh = h / 2.0;

        cloud.moveTo(x + rw, y + h - rh);
        cloud.curveTo(x, y + h - rh, x, y + h, x + rw, y + h); // Bottom-left arc
        cloud.lineTo(x + w - rw, y + h); // Flat bottom
        cloud.curveTo(x + w, y + h, x + w, y + h - rh, x + w - rw, y + h - rh); // Bottom-right
        cloud.curveTo(x + w, y, x + w - rw * 2, y, x + w - rw * 2, y + rh / 2); // Top-right fluff
        cloud.curveTo(x + rw * 1.5, y - rh / 3, x + rw, y + rh / 2, x + rw, y + h - rh); // Top-left fluff
        cloud.closePath();

        // Fill color
        Color fill = dark ? new Color(110, 120, 135) : new Color(245, 245, 250);
        Color stroke = dark ? new Color(85, 95, 110) : new Color(190, 200, 215);

        g2.setColor(fill);
        g2.fill(cloud);

        g2.setColor(stroke);
        g2.setStroke(new BasicStroke(size / 30.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(cloud);
    }

    private void drawRain(Graphics2D g2, int cx, int cy, int size) {
        g2.setColor(new Color(0, 150, 255));
        g2.setStroke(new BasicStroke(size / 25.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        int spacing = size / 5;
        int len = size / 4;
        
        // Draw 3 rain streaks
        for (int i = -1; i <= 1; i++) {
            int rx = cx + i * spacing;
            int ry = cy + (Math.abs(i) * size / 12);
            g2.drawLine(rx, ry, rx - size / 10, ry + len);
        }
    }

    private void drawLightning(Graphics2D g2, int cx, int cy, int size) {
        Path2D.Double lightning = new Path2D.Double();
        int w = size / 2;
        int h = size * 3 / 4;
        int x = cx - w / 2;
        int y = cy - h / 3;

        lightning.moveTo(x + w * 2 / 3, y);
        lightning.lineTo(x, y + h / 2);
        lightning.lineTo(x + w / 2, y + h / 2);
        lightning.lineTo(x + w / 3, y + h);
        lightning.lineTo(x + w, y + h * 5 / 12);
        lightning.lineTo(x + w / 2, y + h * 5 / 12);
        lightning.closePath();

        g2.setColor(new Color(255, 220, 0));
        g2.fill(lightning);

        g2.setColor(new Color(255, 190, 0));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(lightning);
    }

    private void drawSnow(Graphics2D g2, int cx, int cy, int size) {
        g2.setColor(new Color(200, 230, 255));
        g2.setStroke(new BasicStroke(size / 25.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int spacing = size / 4;
        // Draw 3 snowflakes as small stars
        for (int i = -1; i <= 1; i++) {
            int sx = cx + i * spacing;
            int sy = cy + (Math.abs(i) * size / 10);
            int len = size / 6;

            g2.drawLine(sx - len, sy, sx + len, sy);
            g2.drawLine(sx, sy - len, sx, sy + len);
            g2.drawLine(sx - len * 7 / 10, sy - len * 7 / 10, sx + len * 7 / 10, sy + len * 7 / 10);
            g2.drawLine(sx - len * 7 / 10, sy + len * 7 / 10, sx + len * 7 / 10, sy - len * 7 / 10);
        }
    }

    private void drawMist(Graphics2D g2, int cx, int cy, int size) {
        g2.setColor(new Color(160, 180, 200, 180));
        g2.setStroke(new BasicStroke(size / 25.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int w = size * 3 / 4;
        int y1 = cy - size / 4;
        int y2 = cy;
        int y3 = cy + size / 4;

        g2.drawLine(cx - w / 2, y1, cx + w / 2, y1);
        g2.drawLine(cx - w * 3 / 5, y2, cx + w * 3 / 5, y2);
        g2.drawLine(cx - w / 3, y3, cx + w / 3, y3);
    }
}
