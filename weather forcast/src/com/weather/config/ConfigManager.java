package com.weather.config;

import java.io.*;
import java.util.*;

/**
 * Manages saving and loading of application settings, including API keys,
 * preferred unit system, and favorite cities.
 */
public class ConfigManager {
    private static final String CONFIG_FILE = "config.properties";
    private final Properties properties = new Properties();

    public ConfigManager() {
        loadConfig();
    }

    /**
     * Loads settings from the properties file, if it exists.
     */
    public void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                properties.load(is);
                // Auto-migrate from old defaults to Indian cities
                if ("London,New York,Tokyo".equals(properties.getProperty("favoriteCities"))) {
                    properties.setProperty("favoriteCities", "New Delhi,Mumbai,Bengaluru,Kolkata,Chennai,Hyderabad,Shimla");
                    saveConfig();
                }
            } catch (IOException e) {
                System.err.println("Error loading configuration: " + e.getMessage());
            }
        } else {
            // Set defaults
            properties.setProperty("unitSystem", "metric"); // metric = Celsius, imperial = Fahrenheit
            properties.setProperty("apiKey", "");
            properties.setProperty("favoriteCities", "New Delhi,Mumbai,Bengaluru,Kolkata,Chennai,Hyderabad,Shimla");
            saveConfig();
        }
    }

    /**
     * Saves settings to the properties file.
     */
    public synchronized void saveConfig() {
        try (OutputStream os = new FileOutputStream(CONFIG_FILE)) {
            properties.store(os, "Weather Forecast Application Settings");
        } catch (IOException e) {
            System.err.println("Error saving configuration: " + e.getMessage());
        }
    }

    public String getApiKey() {
        return properties.getProperty("apiKey", "").trim();
    }

    public void setApiKey(String apiKey) {
        properties.setProperty("apiKey", apiKey.trim());
        saveConfig();
    }

    public String getUnitSystem() {
        return "metric"; // Enforce Celsius units globally
    }

    public void setUnitSystem(String unitSystem) {
        properties.setProperty("unitSystem", "metric");
        saveConfig();
    }

    public List<String> getFavoriteCities() {
        String favs = properties.getProperty("favoriteCities", "");
        if (favs.isEmpty()) {
            return new ArrayList<>();
        }
        String[] cities = favs.split(",");
        List<String> list = new ArrayList<>();
        for (String city : cities) {
            String trimmed = city.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    public void addFavoriteCity(String city) {
        List<String> list = getFavoriteCities();
        String target = city.trim();
        // Remove duplicates case-insensitively
        list.removeIf(c -> c.equalsIgnoreCase(target));
        list.add(0, target); // Add to the top of the list

        // Limit to 8 favorite cities
        if (list.size() > 8) {
            list = list.subList(0, 8);
        }

        properties.setProperty("favoriteCities", String.join(",", list));
        saveConfig();
    }

    public void removeFavoriteCity(String city) {
        List<String> list = getFavoriteCities();
        String target = city.trim();
        list.removeIf(c -> c.equalsIgnoreCase(target));
        properties.setProperty("favoriteCities", String.join(",", list));
        saveConfig();
    }
}
