package com.weather.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.weather.api.MockWeatherService;
import com.weather.api.OpenWeatherMapService;
import com.weather.api.WeatherService;
import com.weather.api.WeatherService.WeatherException;
import com.weather.config.ConfigManager;
import com.weather.model.WeatherData;
import com.weather.ui.components.WeatherIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Main application window of the Weather Forecast Application.
 * Displays real-time weather details, manage settings, favorites, and themes.
 */
public class WeatherFrame extends JFrame {
    private final ConfigManager configManager;
    private WeatherService weatherService;
    private WeatherData currentWeatherData;

    // UI Components
    private JTextField searchField;
    private JButton searchButton;
    private JButton favoriteButton;
    private JButton themeToggle;
    private JButton settingsButton;

    // Sidebar Favorite List
    private JPanel favoritesPanel;
    private DefaultListModel<String> favoritesListModel;
    private JList<String> favoritesJList;

    // Dashboard Info panels
    private JLabel cityLabel;
    private JLabel timeLabel;
    private JLabel tempLabel;
    private JLabel conditionLabel;
    private JLabel descLabel;
    private WeatherIcon weatherIcon;
    private JPanel weatherCard;

    // Loading Overlay
    private JProgressBar progressBar;

    // Detailed metrics components
    private JLabel feelsLikeVal;
    private JLabel humidityVal;
    private JLabel windVal;
    private JLabel pressureVal;
    private JLabel coordVal;

    // Forecast components
    private JPanel forecastPanel;

    private boolean isDarkMode = true;

    public WeatherFrame() {
        this.configManager = new ConfigManager();
        initializeService();
        setupWindow();
        initUI();

        // Load default city on startup
        List<String> favs = configManager.getFavoriteCities();
        String initialCity = favs.isEmpty() ? "New Delhi" : favs.get(0);
        searchCity(initialCity);
    }

    private void initializeService() {
        String apiKey = configManager.getApiKey();
        if (apiKey.isEmpty()) {
            this.weatherService = new MockWeatherService();
        } else {
            this.weatherService = new OpenWeatherMapService(apiKey);
        }
    }

    private void setupWindow() {
        setTitle("Mausam Weather Forecast - India Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(850, 580));
        setLocationRelativeTo(null);

        // Save config when window closes
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                configManager.saveConfig();
            }
        });
    }

    private void initUI() {
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        // Sidebar - Favorites panel
        setupSidebar(mainPanel);

        // Main Dashboard Area
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.add(dashboard, BorderLayout.CENTER);

        // Header controls (Search, settings, etc.)
        setupHeader(dashboard);

        // Central Content (Scrollable for smaller screens)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dashboard.add(scrollPane, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // 1. Current Weather Card
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0);
        setupWeatherCard(contentPanel, gbc);

        // 2. Metrics Detail Panel
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0);
        setupMetricsGrid(contentPanel, gbc);

        // 3. 3-Day Forecast Section
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        setupForecastSection(contentPanel, gbc);
    }

    private void setupSidebar(JPanel parent) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Component.borderColor")));

        // Sidebar title
        JLabel titleLabel = new JLabel("Favorites");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setBorder(new EmptyBorder(15, 15, 10, 15));
        sidebar.add(titleLabel, BorderLayout.NORTH);

        // Favorites JList setup
        favoritesListModel = new DefaultListModel<>();
        loadFavoritesIntoList();

        favoritesJList = new JList<>(favoritesListModel);
        favoritesJList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        favoritesJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        favoritesJList.setFixedCellHeight(40);
        favoritesJList.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Highlight selected city on click
        favoritesJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedCity = favoritesJList.getSelectedValue();
                if (selectedCity != null) {
                    searchField.setText(selectedCity);
                    searchCity(selectedCity);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(favoritesJList);
        scrollPane.setBorder(null);
        sidebar.add(scrollPane, BorderLayout.CENTER);

        // Manage favorites control panel
        JPanel managePanel = new JPanel(new GridLayout(1, 2, 5, 5));
        managePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton removeBtn = new JButton("Remove Selected");
        removeBtn.putClientProperty("JButton.buttonType", "roundRect");
        removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        removeBtn.addActionListener(e -> {
            String selectedCity = favoritesJList.getSelectedValue();
            if (selectedCity != null) {
                configManager.removeFavoriteCity(selectedCity);
                loadFavoritesIntoList();
                updateFavoriteStarState();
            } else {
                JOptionPane.showMessageDialog(this, "Select a city from the list to remove it.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        managePanel.add(removeBtn);

        sidebar.add(managePanel, BorderLayout.SOUTH);
        parent.add(sidebar, BorderLayout.WEST);
    }

    private void loadFavoritesIntoList() {
        favoritesListModel.clear();
        List<String> favs = configManager.getFavoriteCities();
        for (String city : favs) {
            favoritesListModel.addElement(city);
        }
    }

    private void setupHeader(JPanel parent) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Search Bar Area
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchBarPanel.setOpaque(false);

        searchField = new JTextField(22);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.putClientProperty("JTextField.placeholderText", "Search City (e.g. Paris, JP)...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                searchCity(query);
            }
        });
        searchBarPanel.add(searchField);

        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchButton.putClientProperty("JButton.buttonType", "roundRect");
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                searchCity(query);
            }
        });
        searchBarPanel.add(searchButton);

        // Favorite Toggle Star Button
        favoriteButton = new JButton();
        favoriteButton.setToolTipText("Add/Remove Favorite");
        favoriteButton.setPreferredSize(new Dimension(36, 36));
        favoriteButton.addActionListener(e -> toggleFavoriteCurrentCity());
        searchBarPanel.add(favoriteButton);

        headerPanel.add(searchBarPanel, BorderLayout.WEST);

        // Action controls (Theme switch, unit preference, settings)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        // Progress bar for search loading indicators
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(100, 16));
        actionPanel.add(progressBar);



        // Theme Toggle (Light / Dark)
        themeToggle = new JButton("Dark Mode");
        themeToggle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        themeToggle.addActionListener(e -> toggleTheme());
        actionPanel.add(themeToggle);

        // Settings Dialog Button
        settingsButton = new JButton("Settings");
        settingsButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        settingsButton.addActionListener(e -> showSettingsDialog());
        actionPanel.add(settingsButton);

        headerPanel.add(actionPanel, BorderLayout.EAST);
        parent.add(headerPanel, BorderLayout.NORTH);
    }

    private void setupWeatherCard(JPanel parent, GridBagConstraints gbc) {
        weatherCard = new JPanel(new BorderLayout()) {
            // Give the card a beautiful modern gradient background
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Select colors based on temperature and condition
                Color c1, c2;
                if (currentWeatherData == null) {
                    c1 = new Color(20, 30, 48);
                    c2 = new Color(36, 59, 85);
                } else {
                    double t = currentWeatherData.getTemperature();
                    String cond = currentWeatherData.getCondition().toLowerCase();
                    boolean imperial = "imperial".equalsIgnoreCase(configManager.getUnitSystem());
                    double thresholdHot = imperial ? 82 : 28;
                    double thresholdCold = imperial ? 45 : 7;

                    if (t >= thresholdHot) {
                        c1 = new Color(229, 93, 135); // Warm Sunset Red/Pink
                        c2 = new Color(95, 39, 205);
                    } else if (t <= thresholdCold) {
                        c1 = new Color(43, 192, 228); // Cold Ice Blue
                        c2 = new Color(15, 32, 67);
                    } else if (cond.contains("rain") || cond.contains("storm")) {
                        c1 = new Color(44, 62, 80); // Stormy slate/indigo
                        c2 = new Color(76, 161, 175);
                    } else { // Temperate Clear/Clouds
                        c1 = new Color(0, 180, 219); // Sunny sky cyan/blue
                        c2 = new Color(0, 131, 176);
                    }
                }

                GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                g2.dispose();
            }
        };
        weatherCard.setOpaque(false);
        weatherCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        weatherCard.setPreferredSize(new Dimension(0, 240));

        // Info layout inside card
        JPanel textGroup = new JPanel(new GridLayout(4, 1, 0, 5));
        textGroup.setOpaque(false);

        cityLabel = new JLabel("Loading...");
        cityLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        cityLabel.setForeground(Color.WHITE);
        textGroup.add(cityLabel);

        timeLabel = new JLabel("Local Time");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timeLabel.setForeground(new Color(255, 255, 255, 180));
        textGroup.add(timeLabel);

        conditionLabel = new JLabel("Condition");
        conditionLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        conditionLabel.setForeground(Color.WHITE);
        textGroup.add(conditionLabel);

        descLabel = new JLabel("detailed description");
        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        descLabel.setForeground(new Color(255, 255, 255, 220));
        textGroup.add(descLabel);

        weatherCard.add(textGroup, BorderLayout.WEST);

        // Big Temp and Icon display on the right
        JPanel rightGroup = new JPanel(new BorderLayout(15, 0));
        rightGroup.setOpaque(false);

        tempLabel = new JLabel("--°C");
        tempLabel.setFont(new Font("Outfit", Font.BOLD, 54));
        tempLabel.setForeground(Color.WHITE);
        tempLabel.setVerticalAlignment(SwingConstants.CENTER);
        tempLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightGroup.add(tempLabel, BorderLayout.CENTER);

        weatherIcon = new WeatherIcon(120);
        rightGroup.add(weatherIcon, BorderLayout.EAST);

        weatherCard.add(rightGroup, BorderLayout.EAST);
        parent.add(weatherCard, gbc);
    }

    private void setupMetricsGrid(JPanel parent, GridBagConstraints gbc) {
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        gridPanel.setOpaque(false);

        // Add 5 details panels (Feels Like, Humidity, Wind, Pressure, Coordinates)
        gridPanel.add(createMetricCard("Feels Like", feelsLikeVal = new JLabel("--"), "🌡️"));
        gridPanel.add(createMetricCard("Humidity", humidityVal = new JLabel("--"), "💧"));
        gridPanel.add(createMetricCard("Wind Speed", windVal = new JLabel("--"), "💨"));
        gridPanel.add(createMetricCard("Pressure", pressureVal = new JLabel("--"), "🧭"));
        gridPanel.add(createMetricCard("Coordinates", coordVal = new JLabel("--"), "📍"));

        // A decorative, dynamic tips card
        JLabel mockBadge;
        gridPanel.add(createMetricCard("Status Badge", mockBadge = new JLabel("Offline Mode"), "📡"));
        mockBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));

        parent.add(gridPanel, gbc);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String emoji) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        // Emoji & Title on Top
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        header.setOpaque(false);
        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        header.add(emojiLbl);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        header.add(titleLbl);

        card.add(header, BorderLayout.NORTH);

        // Value in Center
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setBorder(new EmptyBorder(5, 5, 0, 0));
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void setupForecastSection(JPanel parent, GridBagConstraints gbc) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        JLabel title = new JLabel("3-Day Weather Outlook (Simulated)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setBorder(new EmptyBorder(0, 5, 10, 0));
        container.add(title, BorderLayout.NORTH);

        forecastPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        forecastPanel.setOpaque(false);
        
        // Setup empty cards for 3 days
        for (int i = 0; i < 3; i++) {
            forecastPanel.add(createForecastCard("Day " + (i + 1), "01d", "--°C", "Sunny"));
        }
        
        container.add(forecastPanel, BorderLayout.CENTER);
        parent.add(container, gbc);
    }

    private JPanel createForecastCard(String dayName, String iconCode, String temp, String cond) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.NONE;

        // Day Name
        g.gridy = 0;
        JLabel dayLbl = new JLabel(dayName);
        dayLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(dayLbl, g);

        // Icon
        g.gridy = 1;
        g.insets = new Insets(5, 0, 5, 0);
        WeatherIcon icon = new WeatherIcon(50);
        icon.setIconCode(iconCode);
        card.add(icon, g);

        // Temp
        g.gridy = 2;
        g.insets = new Insets(0, 0, 0, 0);
        JLabel tempLbl = new JLabel(temp);
        tempLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        card.add(tempLbl, g);

        // Condition
        g.gridy = 3;
        JLabel condLbl = new JLabel(cond);
        condLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        condLbl.setForeground(UIManager.getColor("Label.disabledForeground"));
        card.add(condLbl, g);

        return card;
    }

    private void updateForecastUI(double currentTemp, String condition, String units) {
        forecastPanel.removeAll();

        String[] days = {"Tomorrow", "Day After", "In 3 Days"};
        String unitSymbol = "metric".equalsIgnoreCase(units) ? "°C" : "°F";

        long seed = currentWeatherData != null ? currentWeatherData.getCityName().hashCode() : 42;
        java.util.Random rand = new java.util.Random(seed);

        for (int i = 0; i < 3; i++) {
            // Calculate a slight variance in temperature
            double offset = -3.0 + (rand.nextDouble() * 6.0) + (i * 0.5);
            double fTemp = currentTemp + offset;

            // Pick a simulated icon and condition based on current weather + random shift
            String fIcon;
            String fCond;
            int roll = rand.nextInt(100);

            if (roll < 40) {
                fIcon = currentWeatherData != null ? currentWeatherData.getIconCode() : "02d";
                fCond = currentWeatherData != null ? currentWeatherData.getCondition() : "Clouds";
            } else if (roll < 70) {
                fIcon = "01d";
                fCond = "Clear";
            } else {
                fIcon = "10d";
                fCond = "Rain";
            }

            forecastPanel.add(createForecastCard(
                    days[i],
                    fIcon,
                    String.format("%.1f%s", fTemp, unitSymbol),
                    fCond
            ));
        }

        forecastPanel.revalidate();
        forecastPanel.repaint();
    }

    /**
     * Executes weather query in a background thread to prevent UI freezing.
     */
    private void searchCity(String city) {
        progressBar.setVisible(true);
        searchButton.setEnabled(false);

        String units = configManager.getUnitSystem();

        // SwingWorker handles asynchronous HTTP fetching safely
        SwingWorker<WeatherData, Void> worker = new SwingWorker<>() {
            private String errorMsg = null;

            @Override
            protected WeatherData doInBackground() throws Exception {
                try {
                    return weatherService.getWeather(city, units);
                } catch (WeatherException e) {
                    errorMsg = e.getMessage();
                    throw e;
                }
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                searchButton.setEnabled(true);

                try {
                    WeatherData data = get();
                    currentWeatherData = data;
                    displayWeather(data);
                    
                    // On successful fetch, optionally auto-add city to history text box
                    searchField.setText(data.getCityName());
                } catch (Exception e) {
                    if (errorMsg != null) {
                        JOptionPane.showMessageDialog(
                                WeatherFrame.this,
                                errorMsg,
                                "Weather Fetch Error",
                                JOptionPane.WARNING_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                WeatherFrame.this,
                                "An unexpected error occurred while fetching weather data.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        };

        worker.execute();
    }

    private void displayWeather(WeatherData data) {
        String units = configManager.getUnitSystem();

        // Left current weather card updates
        cityLabel.setText(data.getCityName() + ", " + data.getCountryCode());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a");
        timeLabel.setText("Fetched: " + sdf.format(new Date(data.getTimestamp() * 1000L)));
        
        conditionLabel.setText(data.getCondition());
        descLabel.setText(data.getDescription().substring(0, 1).toUpperCase() + data.getDescription().substring(1));
        tempLabel.setText(data.getFormattedTemperature(units));
        weatherIcon.setIconCode(data.getIconCode());

        // Grid details panel updates
        feelsLikeVal.setText(data.getFormattedFeelsLike(units));
        humidityVal.setText(data.getHumidity() + "%");
        windVal.setText(data.getFormattedWindSpeed(units));
        pressureVal.setText(data.getPressure() + " hPa");
        coordVal.setText(String.format("Lat: %.2f, Lon: %.2f", data.getLatitude(), data.getLongitude()));

        // Status badge updates
        JPanel badgeCard = (JPanel) feelsLikeVal.getParent().getParent().getComponent(5);
        JLabel badgeVal = (JLabel) badgeCard.getComponent(1);
        if (data.isMock()) {
            badgeVal.setText("Mock Demo Mode");
            badgeVal.setForeground(new Color(230, 140, 10)); // Warning gold
        } else {
            badgeVal.setText("Live API Mode");
            badgeVal.setForeground(new Color(40, 167, 69)); // Success green
        }

        updateFavoriteStarState();
        updateForecastUI(data.getTemperature(), data.getCondition(), units);
        
        // Repaint weatherCard to trigger background color updates based on weather condition
        weatherCard.repaint();
    }

    private void toggleFavoriteCurrentCity() {
        if (currentWeatherData == null) return;
        
        String city = currentWeatherData.getCityName();
        List<String> favs = configManager.getFavoriteCities();
        boolean isFav = false;
        for (String c : favs) {
            if (c.equalsIgnoreCase(city)) {
                isFav = true;
                break;
            }
        }

        if (isFav) {
            configManager.removeFavoriteCity(city);
        } else {
            configManager.addFavoriteCity(city);
        }
        
        loadFavoritesIntoList();
        updateFavoriteStarState();
    }

    private void updateFavoriteStarState() {
        if (currentWeatherData == null) {
            favoriteButton.setText("☆");
            return;
        }

        String city = currentWeatherData.getCityName();
        List<String> favs = configManager.getFavoriteCities();
        boolean isFav = false;
        for (String c : favs) {
            if (c.equalsIgnoreCase(city)) {
                isFav = true;
                break;
            }
        }

        if (isFav) {
            favoriteButton.setText("★");
            favoriteButton.setForeground(new Color(253, 184, 19)); // Gold star
        } else {
            favoriteButton.setText("☆");
            favoriteButton.setForeground(UIManager.getColor("Button.foreground"));
        }
    }



    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        try {
            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                themeToggle.setText("Light Mode");
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
                themeToggle.setText("Dark Mode");
            }
            FlatLaf.updateUI();
        } catch (Exception ex) {
            System.err.println("Failed to toggle look and feel: " + ex.getMessage());
        }
    }

    private void showSettingsDialog() {
        JDialog dialog = new JDialog(this, "Configuration Settings", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(420, 240);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        dialog.add(formPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // 1. API key row
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        formPanel.add(new JLabel("OpenWeatherMap API Key:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField apiKeyField = new JTextField(configManager.getApiKey());
        formPanel.add(apiKeyField, gbc);

        // 2. Info / helper label
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JLabel helperLabel = new JLabel("<html><body style='width: 280px; color: grey; font-size: 9px;'>" +
                "Leave API Key blank to run the application in offline Demo/Mock Mode. " +
                "Get a free key from <i>openweathermap.org</i>.</body></html>");
        formPanel.add(helperLabel, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelBtn);

        JButton saveBtn = new JButton("Save & Apply");
        saveBtn.putClientProperty("JButton.buttonType", "roundRect");
        saveBtn.addActionListener(e -> {
            String newKey = apiKeyField.getText().trim();
            configManager.setApiKey(newKey);
            initializeService(); // Reload weatherService type based on key configuration
            
            dialog.dispose();

            // Refetch current city weather to apply new settings
            if (currentWeatherData != null) {
                searchCity(currentWeatherData.getCityName());
            }
        });
        buttonPanel.add(saveBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
