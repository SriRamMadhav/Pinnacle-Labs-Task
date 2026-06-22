package com.weather.api;

import com.weather.model.WeatherData;

/**
 * Interface defining the API contract for fetching weather data.
 */
public interface WeatherService {
    /**
     * Retrieves the weather for the specified city.
     *
     * @param city  the name of the city (e.g. "London" or "London,UK")
     * @param units the unit system ("metric" or "imperial")
     * @return a WeatherData object containing the current weather
     * @throws WeatherException if weather data cannot be retrieved (network errors, city not found, etc.)
     */
    WeatherData getWeather(String city, String units) throws WeatherException;

    /**
     * Custom exception for weather service errors.
     */
    class WeatherException extends Exception {
        private final boolean isCityNotFound;

        public WeatherException(String message) {
            super(message);
            this.isCityNotFound = false;
        }

        public WeatherException(String message, Throwable cause) {
            super(message, cause);
            this.isCityNotFound = false;
        }

        public WeatherException(String message, boolean isCityNotFound) {
            super(message);
            this.isCityNotFound = isCityNotFound;
        }

        public boolean isCityNotFound() {
            return isCityNotFound;
        }
    }
}
