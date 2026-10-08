package weather;

import weather.cli.WeatherCLI;
import weather.provider.OpenMeteoWeatherProvider;
import weather.provider.WeatherDataProvider;
import weather.service.WeatherService;

import java.net.http.HttpClient;

public class Main {

    public static void main(String[] args) {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            WeatherDataProvider provider = new OpenMeteoWeatherProvider(httpClient);
            WeatherService service = new WeatherService(provider);
            WeatherCLI cli = new WeatherCLI(service);
            cli.run();
        }
    }
}