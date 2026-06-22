package com.weather.model;

/**
 * Represents the weather information for a specific location at a certain time.
 */
public class WeatherData {
    private final String cityName;
    private final String countryCode;
    private final double temperature;
    private final double feelsLike;
    private final double tempMin;
    private final double tempMax;
    private final int humidity;
    private final int pressure;
    private final double windSpeed;
    private final String condition;
    private final String description;
    private final String iconCode;
    private final double latitude;
    private final double longitude;
    private final long timestamp;
    private final boolean isMock;

    public WeatherData(String cityName, String countryCode, double temperature, double feelsLike,
                       double tempMin, double tempMax, int humidity, int pressure, double windSpeed,
                       String condition, String description, String iconCode, double latitude,
                       double longitude, long timestamp, boolean isMock) {
        this.cityName = cityName;
        this.countryCode = countryCode;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.condition = condition;
        this.description = description;
        this.iconCode = iconCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.isMock = isMock;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public double getTempMin() {
        return tempMin;
    }

    public double getTempMax() {
        return tempMax;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getCondition() {
        return condition;
    }

    public String getDescription() {
        return description;
    }

    public String getIconCode() {
        return iconCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isMock() {
        return isMock;
    }

    /**
     * Helper to get a formatted temperature string (e.g. "23°C" or "74°F").
     */
    public String getFormattedTemperature(String unitSystem) {
        String unit = "metric".equalsIgnoreCase(unitSystem) ? "°C" : "°F";
        return String.format("%.1f%s", temperature, unit);
    }

    public String getFormattedFeelsLike(String unitSystem) {
        String unit = "metric".equalsIgnoreCase(unitSystem) ? "°C" : "°F";
        return String.format("%.1f%s", feelsLike, unit);
    }

    public String getFormattedWindSpeed(String unitSystem) {
        String unit = "metric".equalsIgnoreCase(unitSystem) ? "m/s" : "mph";
        return String.format("%.1f %s", windSpeed, unit);
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "cityName='" + cityName + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", temperature=" + temperature +
                ", condition='" + condition + '\'' +
                ", description='" + description + '\'' +
                ", isMock=" + isMock +
                '}';
    }
}
