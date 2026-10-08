package weather.service;

import weather.model.Location;
import weather.model.WeatherData;
import weather.provider.WeatherDataProvider;
import weather.provider.WeatherDataProvider.WeatherProviderException;


import java.util.List;

public class WeatherService{
    private final WeatherDataProvider provider;
    private final List<Location> locations = List.of(
            new Location("Chicago", 41.85, -87.65),
            new Location("Los Angeles", 34.05, -118.24),
            new Location("New York", 40.71, -74.01)
    );

    public WeatherService(WeatherDataProvider provider){
        this.provider= provider;
    }

    public static class UnknownLocationException extends Exception{
        public UnknownLocationException(String name){
            super("Unknown location: "+ name);
        }
    }
    public List<Location> getLocations() {
        return locations;
    }
    private Location findLocation(String name) throws UnknownLocationException{
        if (name== null|| name.isBlank()){
            throw new UnknownLocationException(String.valueOf(name));
        }
        for (Location location :locations){

            if(location.name().equalsIgnoreCase(name.strip())){
                return location;
            }
        }
        throw new UnknownLocationException(name);
    }
    public WeatherData getCurrentWeather(String name)
            throws UnknownLocationException, WeatherProviderException {
        Location location = findLocation(name);
        return provider.getCurrentWeather(location);
    }

    public record Comparison(WeatherData first, WeatherData second) {

        public double temperatureDifference() {
            return first.temperatureF() - second.temperatureF();
        }

        public int precipitationDifference() {
            return (first.precipitationProbabilityPercent() - second.precipitationProbabilityPercent());
        }

        public double windSpeedDifference() {
            return first.windSpeedMph() - second.windSpeedMph();
        }

        public String warmerCity() {
            if(first.temperatureF() > second.temperatureF()){
                return first.location().name();
            }else if(first.temperatureF() < second.temperatureF()){
                return second.location().name();
            }else{
                return "Neither";
            }
        }
    }

    public Comparison compare(String firstName, String secondName)
            throws UnknownLocationException, WeatherProviderException {

        Location location = findLocation(firstName);
        Location location2 = findLocation(secondName);
        WeatherData firstData = provider.getCurrentWeather(location);
        WeatherData secondData = provider.getCurrentWeather(location2);
        return new Comparison(firstData, secondData);

    }

    public String biggestTemperatureChange() throws WeatherProviderException {
        Location biggestCity = null;
        double biggestChange = -1;

        for (Location location : locations) {
            WeatherData data = provider.getCurrentWeather(location);
            double change = data.highF() - data.lowF();
            if (change > biggestChange) {
                biggestChange = change;
                biggestCity = location;
            }
        }
        return "%s has the biggest temperature change today: %.1f°F".formatted(biggestCity.name(), biggestChange);
    }

    public String summarize(String name) throws UnknownLocationException, WeatherProviderException {
        WeatherData data = getCurrentWeather(name);

        String precip = describePrecipitation(data.precipitationProbabilityPercent());
        String precipSentence = Character.toUpperCase(precip.charAt(0)) + precip.substring(1);

        return "In %s it is currently %.0f°F and %s, with %s at %.0f mph. %s (%d%% chance)."
                .formatted(
                        data.location().name(),
                        data.temperatureF(),
                        describeTemperature(data.temperatureF()),
                        describeWind(data.windSpeedMph()),
                        data.windSpeedMph(),
                        precipSentence,
                        data.precipitationProbabilityPercent());
    }

    private static String describeTemperature(double f) {
        // < 32 "freezing", < 50 "cold", < 65 "cool", < 80 "pleasant", < 90 "warm", else "hot"
        if(f<32){
            return "freezing";
        }else if(f<50){
            return "cold";
        }else if(f<65){
            return "cool";
        }else if(f<80){
            return "pleasant";
        }else if(f<90){
            return "warm";
        }
        return "hot";
    }

    private static String describePrecipitation(int pct) {
        //  < 20 "rain is unlikely", < 50 "there is a slight chance of rain", < 80 "rain is likely", else "rain is very likely"
        if(pct<20){
            return "rain is unlikely";
        }else if(pct<50) {
            return "there is a slight chance of rain";
        }else if(pct<80) {
            return "rain is likely";
        }
        return "rain is very likely";
    }

    private static String describeWind(double mph) {
        // < 1 "calm air", < 10 "a light breeze", < 20 "a steady breeze", else "strong winds"
        if(mph<1){
            return "calm air";
        }else if(mph<10){
            return "a light breeze";
        }else if(mph<20){
            return "a steady breeze";
        }
        return "strong winds";
    }

}
