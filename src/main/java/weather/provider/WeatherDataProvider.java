package weather.provider;

import weather.model.Location;
import weather.model.WeatherData;

public interface WeatherDataProvider {
    WeatherData getCurrentWeather(Location location) throws WeatherProviderException;

    class WeatherProviderException extends Exception {

        public WeatherProviderException(String message) {
            super(message);
        }

        public WeatherProviderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}