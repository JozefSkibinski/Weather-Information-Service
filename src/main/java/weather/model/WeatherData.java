package weather.model;

import java.util.Objects;

public record WeatherData (Location location, double temperatureF, int precipitationProbabilityPercent, double windSpeedMph, double highF, double lowF){

    // slightly below and above record low and high
    private static final double MIN_TEMP_F = -130.0;
    private static final double MAX_TEMP_F = 140.0;

    public WeatherData {
        Objects.requireNonNull(location, "location must not be null");

        if (Double.isNaN(temperatureF) || temperatureF < MIN_TEMP_F || temperatureF > MAX_TEMP_F){
            throw new IllegalArgumentException("Temperature out of range (" + MIN_TEMP_F + " to " + MAX_TEMP_F + " °F): " + temperatureF);
        }

        if (precipitationProbabilityPercent < 0 || precipitationProbabilityPercent > 100) {
            throw new IllegalArgumentException("Precipitation probability must be 0-100: " + precipitationProbabilityPercent);
        }

        if (Double.isNaN(windSpeedMph) || windSpeedMph < 0) {
            throw new IllegalArgumentException("Wind speed must be non-negative: " + windSpeedMph);
        }

        // todays high and low, used for the delta command
        if (Double.isNaN(highF) || highF < MIN_TEMP_F || highF > MAX_TEMP_F) {
            throw new IllegalArgumentException("High temperature out of range: " + highF);
        }

        if (Double.isNaN(lowF) || lowF < MIN_TEMP_F || lowF > MAX_TEMP_F) {
            throw new IllegalArgumentException("Low temperature out of range: " + lowF);
        }

        if (lowF > highF) {
            throw new IllegalArgumentException("Low (" + lowF + ") can't be higher than high (" + highF + ")");
        }

    }

}
