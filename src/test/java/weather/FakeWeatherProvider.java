package weather;

import weather.model.Location;
import weather.model.WeatherData;
import weather.provider.WeatherDataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// internet-less tests
public class FakeWeatherProvider implements WeatherDataProvider {

    // city name -> data to return
    private final Map<String, WeatherData> responses = new HashMap<>();
    // city name -> error message to throw
    private final Map<String, String> failures = new HashMap<>();
    // every location we got asked for, so tests can check if it was called
    private final List<Location> requests = new ArrayList<>();

    public WeatherData getCurrentWeather(Location location) throws WeatherProviderException {
        requests.add(location);

        if (failures.containsKey(location.name())) {
            throw new WeatherProviderException(failures.get(location.name()));
        }

        WeatherData data = responses.get(location.name());
        if (data == null) {
            // no fake data set up, throw instead of making something up
            throw new WeatherProviderException("No fake data for " + location.name());
        }
        return data;
    }

    public FakeWeatherProvider addResponse(WeatherData data) {
        responses.put(data.location().name(), data);
        return this;
    }

    public FakeWeatherProvider addFailure(String cityName, String message) {
        failures.put(cityName, message);
        return this;
    }

    public List<Location> getRequests() {
        return requests;
    }

    // same 3 cities as the service with fixed numbers so tests can check exact results
    // temp, rain %, wind mph, high, low
    public static FakeWeatherProvider withSampleData() {
        return new FakeWeatherProvider()
                .addResponse(new WeatherData(new Location("Chicago", 41.85, -87.65), 60.0, 10, 8.0, 65.0, 50.0))
                .addResponse(new WeatherData(new Location("Los Angeles", 34.05, -118.24), 75.0, 0, 3.0, 85.0, 60.0))
                .addResponse(new WeatherData(new Location("New York", 40.71, -74.01), 68.0, 40, 15.0, 72.0, 62.0));
    }
}