package weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import weather.model.Location;
import weather.model.WeatherData;
import weather.provider.WeatherDataProvider;
import weather.provider.WeatherDataProvider.WeatherProviderException;
import weather.service.WeatherService;
import weather.service.WeatherService.Comparison;
import weather.service.WeatherService.UnknownLocationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class WeatherServiceTest {

    private FakeWeatherProvider fake;
    private WeatherService service;

    // fresh fake + service before every test
    @BeforeEach
    void setUp() {
        fake = FakeWeatherProvider.withSampleData();
        service = new WeatherService(fake);
    }

    @Test
    void knownLocationReturnsProviderData() throws Exception {
        WeatherData data = service.getCurrentWeather("Chicago");

        assertEquals("Chicago", data.location().name());
        assertEquals(60.0, data.temperatureF());
        assertEquals(10, data.precipitationProbabilityPercent());
        assertEquals(8.0, data.windSpeedMph());
    }

    @Test
    void lookupIgnoresCaseAndSpaces() throws Exception {
        WeatherData data = service.getCurrentWeather("  new YORK ");

        assertEquals("New York", data.location().name());
    }

    @Test
    void unknownLocationThrowsAndNeverCallsProvider() {
        WeatherDataProvider mockProvider = mock(WeatherDataProvider.class);
        WeatherService mockService = new WeatherService(mockProvider);

        assertThrows(UnknownLocationException.class, () -> mockService.getCurrentWeather("Paris"));
        verifyNoInteractions(mockProvider);
    }

    @Test
    void providerFailurePropagates() {
        fake.addFailure("Chicago", "service down");

        WeatherProviderException e = assertThrows(WeatherProviderException.class,
                () -> service.getCurrentWeather("Chicago"));
        assertEquals("service down", e.getMessage());
    }

    @Test
    void compareComputesDifferencesAndWarmerCity() throws Exception {
        Comparison c = service.compare("Chicago", "Los Angeles");

        assertEquals(-15.0, c.temperatureDifference());   // 60 - 75
        assertEquals(10, c.precipitationDifference());    // 10 - 0
        assertEquals(5.0, c.windSpeedDifference());       // 8 - 3
        assertEquals("Los Angeles", c.warmerCity());
    }

    @Test
    void compareWithUnknownCityMakesZeroProviderCalls() {
        assertThrows(UnknownLocationException.class, () -> service.compare("Chicago", "Paris"));
        assertTrue(fake.getRequests().isEmpty());
    }

    @Test
    void summaryTextIsCorrect() throws Exception {
        assertEquals(
                "In Chicago it is currently 60°F and cool, with a light breeze at 8 mph. Rain is unlikely (10% chance).",
                service.summarize("Chicago"));
    }

    @Test
    void summaryChangesWithConditions() throws Exception {
        // cold, windy, rainy chicago and hot, calm LA
        fake.addResponse(new WeatherData(new Location("Chicago", 41.85, -87.65), 20.0, 90, 25.0, 25.0, 15.0));
        fake.addResponse(new WeatherData(new Location("Los Angeles", 34.05, -118.24), 95.0, 0, 0.5, 100.0, 70.0));

        String cold = service.summarize("Chicago");
        assertTrue(cold.contains("freezing"));
        assertTrue(cold.contains("strong winds"));
        assertTrue(cold.contains("Rain is very likely"));

        String hot = service.summarize("Los Angeles");
        assertTrue(hot.contains("hot"));
        assertTrue(hot.contains("calm air"));
    }

    // our delta feature instead of ranking
    @Test
    void deltaPicksCityWithBiggestChange() throws Exception {
        // chicago 65-50=15, LA 85-60=25, NY 72-62=10
        assertEquals("Los Angeles has the biggest temperature change today: 25.0°F",
                service.biggestTemperatureChange());
    }

    @Test
    void deltaFailsIfOneCityFails() {
        fake.addFailure("New York", "service down");

        assertThrows(WeatherProviderException.class, () -> service.biggestTemperatureChange());
    }
}