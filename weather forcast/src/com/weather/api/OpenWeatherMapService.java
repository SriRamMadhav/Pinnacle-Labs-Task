package com.weather.api;

import com.weather.json.JsonParser;
import com.weather.model.WeatherData;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of {@link WeatherService} that calls the OpenWeatherMap API,
 * parses the response with our custom {@link JsonParser}, and handles network/API errors.
 */
public class OpenWeatherMapService implements WeatherService {
    private final String apiKey;
    private final HttpClient client;

    public OpenWeatherMapService(String apiKey) {
        this.apiKey = apiKey;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
    }

    @Override
    public WeatherData getWeather(String city, String units) throws WeatherException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new WeatherException("API Key is not configured. Please enter it in the settings.", false);
        }
        if (city == null || city.trim().isEmpty()) {
            throw new WeatherException("City name cannot be empty.", true);
        }

        try {
            String encodedCity = URLEncoder.encode(city.trim(), StandardCharsets.UTF_8);
            String urlString = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=%s",
                    encodedCity, apiKey.trim(), units
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .GET()
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode == 200) {
                return parseWeatherData(responseBody);
            } else if (statusCode == 404) {
                throw new WeatherException("City not found: '" + city + "'", true);
            } else if (statusCode == 401) {
                throw new WeatherException("Invalid API Key. Please verify your OpenWeatherMap key in Settings.", false);
            } else {
                // Try parsing the error message from the response if available
                String errorMsg = "HTTP error code: " + statusCode;
                try {
                    Map<String, Object> errMap = JsonParser.parse(responseBody);
                    if (errMap.containsKey("message")) {
                        errorMsg = errMap.get("message").toString();
                    }
                } catch (Exception ignored) {}
                
                throw new WeatherException("API Error: " + errorMsg);
            }

        } catch (WeatherException e) {
            throw e;
        } catch (java.io.IOException e) {
            throw new WeatherException("Network error: Failed to connect to Weather Service. Check your internet connection.", e);
        } catch (Exception e) {
            throw new WeatherException("Failed to fetch weather data: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the raw JSON response from OpenWeatherMap using the custom JsonParser.
     */
    @SuppressWarnings("unchecked")
    private WeatherData parseWeatherData(String json) throws WeatherException {
        try {
            Map<String, Object> root = JsonParser.parse(json);

            // Extract core fields
            String cityName = root.get("name").toString();
            long timestamp = getLongValue(root.get("dt"));

            // Extract coordinates
            Map<String, Object> coord = (Map<String, Object>) root.get("coord");
            double lat = getDoubleValue(coord.get("lat"));
            double lon = getDoubleValue(coord.get("lon"));

            // Extract main measurements
            Map<String, Object> main = (Map<String, Object>) root.get("main");
            double temp = getDoubleValue(main.get("temp"));
            double feelsLike = getDoubleValue(main.get("feels_like"));
            double tempMin = getDoubleValue(main.get("temp_min"));
            double tempMax = getDoubleValue(main.get("temp_max"));
            int humidity = getIntValue(main.get("humidity"));
            int pressure = getIntValue(main.get("pressure"));

            // Extract wind
            Map<String, Object> wind = (Map<String, Object>) root.get("wind");
            double windSpeed = getDoubleValue(wind.get("speed"));

            // Extract country from sys
            Map<String, Object> sys = (Map<String, Object>) root.get("sys");
            String countryCode = sys != null && sys.containsKey("country") ? sys.get("country").toString() : "";

            // Extract weather description
            List<Object> weatherList = (List<Object>) root.get("weather");
            String condition = "Unknown";
            String description = "no description";
            String iconCode = "01d";

            if (weatherList != null && !weatherList.isEmpty()) {
                Map<String, Object> weatherObj = (Map<String, Object>) weatherList.get(0);
                condition = weatherObj.get("main").toString();
                description = weatherObj.get("description").toString();
                iconCode = weatherObj.get("icon").toString();
            }

            return new WeatherData(
                    cityName,
                    countryCode,
                    temp,
                    feelsLike,
                    tempMin,
                    tempMax,
                    humidity,
                    pressure,
                    windSpeed,
                    condition,
                    description,
                    iconCode,
                    lat,
                    lon,
                    timestamp,
                    false // isMock = false (live data)
            );

        } catch (Exception e) {
            throw new WeatherException("Error parsing weather JSON response: " + e.getMessage(), e);
        }
    }

    private double getDoubleValue(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        return 0.0;
    }

    private int getIntValue(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return 0;
    }

    private long getLongValue(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return 0L;
    }
}
