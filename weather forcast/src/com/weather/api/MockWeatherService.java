package com.weather.api;

import com.weather.model.WeatherData;
import java.util.Random;

/**
 * A mock implementation of {@link WeatherService} that generates realistic,
 * consistent weather data for any city name using hash-based seeding.
 * Useful for offline demos or when an OpenWeatherMap API key is not configured.
 */
public class MockWeatherService implements WeatherService {

    @Override
    public WeatherData getWeather(String city, String units) throws WeatherException {
        if (city == null || city.trim().isEmpty()) {
            throw new WeatherException("City name cannot be empty", true);
        }

        String cleanedCity = city.trim();
        
        // Simulating some delay for network realism
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check for test-fail cities to demonstrate error handling
        if (cleanedCity.equalsIgnoreCase("error") || cleanedCity.equalsIgnoreCase("invalid")) {
            throw new WeatherException("City not found (Mock error: City does not exist)", true);
        }
        if (cleanedCity.equalsIgnoreCase("offline") || cleanedCity.equalsIgnoreCase("timeout")) {
            throw new WeatherException("Network error: Connection timed out (Mock network failure)");
        }

        // Use hashCode of city name to generate consistent weather
        long seed = cleanedCity.toLowerCase().hashCode();
        Random random = new Random(seed);

        // Determine climate zone based on city name matching
        int climateZone; // 0 = Cold Himalayan, 1 = Temperate/Pleasant, 2 = Hot Coastal/Inland, 3 = Desert Dry, 4 = Coastal Humid Monsoon
        String lowerCity = cleanedCity.toLowerCase();
        
        if (lowerCity.contains("shimla") || lowerCity.contains("srinagar") || lowerCity.contains("leh") || 
            lowerCity.contains("manali") || lowerCity.contains("kashmir") || lowerCity.contains("ladakh") || 
            lowerCity.contains("darjeeling") || lowerCity.contains("uttarakhand") || lowerCity.contains("himachal") ||
            lowerCity.contains("sikkim") || lowerCity.contains("arunachal") || lowerCity.contains("ooty") ||
            lowerCity.contains("coorg") || lowerCity.contains("kodagu") || lowerCity.contains("dharamshala")) {
            climateZone = 0; // Cold Himalayan
        } else if (lowerCity.contains("jaipur") || lowerCity.contains("rajasthan") || lowerCity.contains("jodhpur") || 
                   lowerCity.contains("jaisalmer") || lowerCity.contains("ahmedabad") || lowerCity.contains("gujarat") ||
                   lowerCity.contains("delhi") || lowerCity.contains("ncr") || lowerCity.contains("noida") || 
                   lowerCity.contains("gurgaon") || lowerCity.contains("punjab") || lowerCity.contains("haryana") || 
                   lowerCity.contains("lucknow") || lowerCity.contains("kanpur") || lowerCity.contains("agra") || 
                   lowerCity.contains("uttar pradesh") || lowerCity.contains("up") || lowerCity.contains("bhopal") ||
                   lowerCity.contains("madhya pradesh") || lowerCity.contains("mp") || lowerCity.contains("chhattisgarh") ||
                   lowerCity.contains("raipur")) {
            climateZone = 3; // Desert / North dry heat
        } else if (lowerCity.contains("mumbai") || lowerCity.contains("maharashtra") || lowerCity.contains("kochi") || 
                   lowerCity.contains("kerala") || lowerCity.contains("goa") || lowerCity.contains("kolkata") || 
                   lowerCity.contains("west bengal") || lowerCity.contains("assam") || lowerCity.contains("meghalaya") ||
                   lowerCity.contains("odisha") || lowerCity.contains("bhubaneswar") || lowerCity.contains("bihar") ||
                   lowerCity.contains("patna") || lowerCity.contains("jharkhand") || lowerCity.contains("ranchi") ||
                   lowerCity.contains("nagpur")) {
            climateZone = 4; // Coastal Humid Monsoon
        } else if (lowerCity.contains("bengaluru") || lowerCity.contains("bangalore") || lowerCity.contains("karnataka") || 
                   lowerCity.contains("mysore") || lowerCity.contains("mysuru") || lowerCity.contains("pune")) {
            climateZone = 1; // Temperate/Pleasant
        } else if (lowerCity.contains("chennai") || lowerCity.contains("tamil nadu") || lowerCity.contains("coimbatore") || 
                   lowerCity.contains("madurai") || lowerCity.contains("andhra") || lowerCity.contains("telangana") || 
                   lowerCity.contains("hyderabad") || lowerCity.contains("visakhapatnam")) {
            climateZone = 2; // Hot Coastal / Southern Inland
        } else {
            // Default hash-based selector for general cities
            climateZone = Math.abs(random.nextInt()) % 3; // 0 = Cold, 1 = Temperate, 2 = Hot
        }
        
        double baseTemp; // in Celsius
        String condition;
        String description;
        String iconCode;
        
        int weatherRoll = random.nextInt(100);
        
        if (climateZone == 0) { // Cold Himalayan/Hill Station (e.g. Shimla, Srinagar, Ladakh)
            if (lowerCity.contains("leh") || lowerCity.contains("ladakh")) {
                baseTemp = 7.0 + (random.nextDouble() * 9.0); // 7C to 16C (Realistic Leh summer)
            } else {
                baseTemp = 13.0 + (random.nextDouble() * 9.0); // 13C to 22C (Shimla/Srinagar June temp)
            }
            if (weatherRoll < 25) {
                condition = "Rain";
                description = "light rain showers";
                iconCode = "10d";
            } else if (weatherRoll < 75) {
                condition = "Clouds";
                description = "cool overcast clouds";
                iconCode = "04d";
            } else {
                condition = "Clear";
                description = "clear sky and pleasant breeze";
                iconCode = "01d";
            }
        } else if (climateZone == 3) { // Desert & North India Dry Hot (e.g. Rajasthan, Delhi, UP)
            baseTemp = 38.0 + (random.nextDouble() * 6.5); // 38C to 44.5C (Realistic severe June heatwave)
            if (weatherRoll < 15) {
                condition = "Atmosphere";
                description = "hazy dust storm";
                iconCode = "50d";
            } else if (weatherRoll < 40) {
                condition = "Clouds";
                description = "hazy sunshine";
                iconCode = "50d";
            } else {
                condition = "Clear";
                description = "scorching sunny day";
                iconCode = "01d";
            }
        } else if (climateZone == 4) { // Coastal Humid & Monsoon Zone (e.g. Mumbai, Kerala, West Bengal)
            baseTemp = 26.5 + (random.nextDouble() * 5.0); // 26.5C to 31.5C (Typical monsoon temperature in June)
            if (weatherRoll < 60) {
                condition = "Rain";
                description = "heavy monsoon downpour";
                iconCode = "10d";
            } else if (weatherRoll < 85) {
                condition = "Thunderstorm";
                description = "thunderstorm with heavy rain";
                iconCode = "11d";
            } else {
                condition = "Clouds";
                description = "humid overcast clouds";
                iconCode = "04d";
            }
        } else if (climateZone == 1) { // Temperate/Pleasant (e.g. Bengaluru, Pune)
            baseTemp = 20.0 + (random.nextDouble() * 7.0); // 20C to 27C (Pleasant June temperatures)
            if (weatherRoll < 30) {
                condition = "Rain";
                description = "light monsoon drizzle";
                iconCode = "09d";
            } else if (weatherRoll < 75) {
                condition = "Clouds";
                description = "pleasant scattered clouds";
                iconCode = "03d";
            } else {
                condition = "Clear";
                description = "cool clear sky";
                iconCode = "01d";
            }
        } else { // Hot Coastal/Inland Southern (e.g. Chennai, Hyderabad)
            baseTemp = 32.0 + (random.nextDouble() * 7.0); // 32C to 39C
            if (weatherRoll < 20) {
                condition = "Rain";
                description = "passing shower";
                iconCode = "09d";
            } else if (weatherRoll < 55) {
                condition = "Clouds";
                description = "partly cloudy and humid";
                iconCode = "03d";
            } else {
                condition = "Clear";
                description = "hot and humid clear sky";
                iconCode = "01d";
            }
        }

        // Apply temperature unit conversions
        double temperature = baseTemp;
        double feelsLikeOffset = -1.0 + (random.nextDouble() * 3.0);
        
        int humidity;
        if (climateZone == 3) {
            humidity = 10 + random.nextInt(16); // 10% to 25% dry
            feelsLikeOffset = -2.0; // Feels cooler than temperature due to lack of humidity
        } else if (climateZone == 4) {
            humidity = 70 + random.nextInt(26); // 70% to 95% humid
            feelsLikeOffset = 4.0 + (random.nextDouble() * 3.0); // Feels much hotter (heat index)
        } else {
            humidity = 35 + random.nextInt(46); // 35% to 80%
        }
        
        double feelsLike = baseTemp + feelsLikeOffset;
        double windSpeed = 1.0 + (random.nextDouble() * 9.0); // m/s
        
        if ("imperial".equalsIgnoreCase(units)) {
            temperature = (baseTemp * 9.0 / 5.0) + 32.0;
            feelsLike = (feelsLike * 9.0 / 5.0) + 32.0;
            windSpeed = windSpeed * 2.23694; // m/s to mph
        }

        int pressure = 995 + random.nextInt(25); // hPa
        
        String countryCode = getCountryCodeFromCity(cleanedCity, random);
        long timestamp = System.currentTimeMillis() / 1000L;

        // Coordinates: Specific bounds for India (Lat: 8N-36N, Lon: 68E-96E) if countryCode is IN
        double latitude;
        double longitude;
        if ("IN".equals(countryCode)) {
            latitude = 8.4 + (random.nextDouble() * 27.6); // 8.4 to 36.0
            longitude = 68.7 + (random.nextDouble() * 27.3); // 68.7 to 96.0
        } else {
            latitude = -60.0 + (random.nextDouble() * 120.0);
            longitude = -120.0 + (random.nextDouble() * 240.0);
        }

        return new WeatherData(
                formatCityName(cleanedCity),
                countryCode,
                temperature,
                feelsLike,
                temperature - 2.0,
                temperature + 2.0,
                humidity,
                pressure,
                windSpeed,
                condition,
                description,
                iconCode,
                latitude,
                longitude,
                timestamp,
                true // isMock
        );
    }

    private String getCountryCodeFromCity(String city, Random random) {
        String lower = city.toLowerCase();
        if (lower.contains("london")) return "GB";
        if (lower.contains("paris")) return "FR";
        if (lower.contains("berlin")) return "DE";
        if (lower.contains("tokyo")) return "JP";
        if (lower.contains("sydney")) return "AU";
        if (lower.contains("cairo")) return "EG";
        if (lower.contains("york") || lower.contains("angeles") || lower.contains("chicago")) return "US";
        
        // Default to India (IN) for an Indian-themed application
        return "IN";
    }

    private String formatCityName(String city) {
        if (city.isEmpty()) return city;
        String[] parts = city.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)))
              .append(parts[i].substring(1).toLowerCase());
            if (i < parts.length - 1) sb.append(" ");
        }
        return sb.toString();
    }
}
