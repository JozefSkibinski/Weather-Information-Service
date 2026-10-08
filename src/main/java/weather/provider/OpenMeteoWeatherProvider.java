package weather.provider;

import java.io.IOException;
import java.util.Locale;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weather.model.Location;
import weather.model.WeatherData;

public class OpenMeteoWeatherProvider implements WeatherDataProvider{
    private final HttpClient client;

    public OpenMeteoWeatherProvider(HttpClient client) {
        this.client = client;
    }

    public WeatherData getCurrentWeather(Location location) throws WeatherProviderException {
        String url = String.format(Locale.ROOT, "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f", location.latitude(), location.longitude()) + "&current=temperature_2m,precipitation_probability,wind_speed_10m" + "&daily=temperature_2m_max,temperature_2m_min&forecast_days=1" + "&temperature_unit=fahrenheit&wind_speed_unit=mph&timezone=auto";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200){
                String current = parseCurrent(response.body());
                double temp = parseField(current, "temperature_2m");
                int precipitation = (int) parseField(current, "precipitation_probability");
                double windSpeed = parseField(current, "wind_speed_10m");
                double high = parseField(response.body(), "temperature_2m_max");
                double low = parseField(response.body(), "temperature_2m_min");
                return new WeatherData(location, temp, precipitation, windSpeed, high, low);
            } else {
                throw new WeatherProviderException(location.name() + " API Error: Code " + response.statusCode() + parseReason(response.body()));
            }
        } catch (IOException except) {
            throw new WeatherProviderException("Could not reach Open-Meteo for " + location.name(), except);
        } catch (InterruptedException except) {
            Thread.currentThread().interrupt();
            throw new WeatherProviderException("Interrupted while fetching weather for " + location.name(), except);
        } catch (IllegalArgumentException except) {
            throw new WeatherProviderException("Invalid weather data for " + location.name() + ": " + except.getMessage(), except);
        }
    }

    // grabs just the "current" part so we dont accidentally read current_units
    // looks like {"current_units":{...},"current":{"temperature_2m":70.0}} -> "temperature_2m":70.0
    static String parseCurrent(String json) throws WeatherProviderException {
        // finds "current": { ... } and takes whats inside the brackets
        Pattern pattern = Pattern.compile("\"current\":\\s*\\{([^}]*)}");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new WeatherProviderException("Response has no \"current\" section");
    }

    // like parseTemperature from main but throws instead of returning 72
    // like "wind_speed_10m":7.9 -> 7.9
    static double parseField(String json, String field) throws WeatherProviderException {
        // finds "field": and grabs the number after it (null wont match)
        // [ is optional bc daily values come like "temperature_2m_max":[71.0]
        Pattern pattern = Pattern.compile("\"" + field + "\":\\s*\\[?([0-9.-]+)");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        // missing or null
        throw new WeatherProviderException("Missing or null field: " + field);
    }

    // gets the error message from the api if there is one, otherwise empty string
    // like {"error":true,"reason":"bad latitude"} -> " (bad latitude)"
    static String parseReason(String json) {
        Pattern pattern = Pattern.compile("\"reason\":\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return " (" + matcher.group(1) + ")";
        }
        return "";
    }

}
