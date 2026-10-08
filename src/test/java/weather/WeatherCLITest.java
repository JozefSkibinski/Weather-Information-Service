package weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import weather.cli.WeatherCLI;
import weather.cli.WeatherCLI.CommandResult;
import weather.service.WeatherService;

import static org.junit.jupiter.api.Assertions.*;

class WeatherCLITest {

    private FakeWeatherProvider fake;
    private WeatherCLI cli;

    // cli -> real service -> fake provider, so no internet
    @BeforeEach
    void setUp() {
        fake = FakeWeatherProvider.withSampleData();
        cli = new WeatherCLI(new WeatherService(fake));
    }

    @Test
    void unknownCommandShowsError() {
        CommandResult result = cli.execute("blah");

        assertTrue(result.output().contains("Unknown command"));
        assertFalse(result.exit());
    }

    @Test
    void missingArgumentShowsUsage() {
        CommandResult result = cli.execute("current");

        assertEquals("Usage: current <location>", result.output());
    }

    @Test
    void currentShowsTemperatureAndExtraMeasurements() {
        // no quotes on purpose, current should join the words
        String output = cli.execute("current New York").output();

        assertTrue(output.contains("New York"));
        assertTrue(output.contains("68.0°F"));
        assertTrue(output.contains("40%"));
        assertTrue(output.contains("15.0 mph"));
    }

    @Test
    void compareWorksWithQuotes() {
        String output = cli.execute("compare Chicago \"Los Angeles\"").output();

        assertTrue(output.contains("Chicago"));
        assertTrue(output.contains("Los Angeles"));
        assertTrue(output.contains("-15.0°F"));
        assertTrue(output.contains("Warmer: Los Angeles"));
    }

    @Test
    void unknownLocationShowsErrorAndKnownLocations() {
        String output = cli.execute("current Paris").output();

        assertTrue(output.contains("Unknown location: Paris"));
        assertTrue(output.contains("Known locations: Chicago, Los Angeles, New York"));
    }

    @Test
    void providerFailureShowsUnavailable() {
        fake.addFailure("Chicago", "service down");

        String output = cli.execute("current Chicago").output();

        assertTrue(output.contains("Weather data unavailable"));
        assertTrue(output.contains("service down"));
    }

    @Test
    void quitExits() {
        CommandResult result = cli.execute("quit");

        assertTrue(result.exit());
        assertEquals("Goodbye!", result.output());
    }
}