# Weather Information Service — Jozef Skibinski & Nathan Parikh

## Requirements

- Java 25
- Maven
- JUnit Jupiter

## Compile

```bash
mvn compile
```

## Run 

```bash
java -cp target/classes weather.Main
```

## Run Tests

```bash
mvn test
```

## Design
- **model**: `Location` and `WeatherData` hold the data.
- **provider**: `OpenMeteoWeatherProvider` gets weather from the API.
- **service**: `WeatherService` handles the logic.
- **cli**: `WeatherCLI` reads commands and shows results.
- **Main**: connects everything together.

## Interfaces
`WeatherDataProvider` is the interface so the app can use the real API while tests run on the fake one.

## Dependency Injection
`WeatherService` gets its `WeatherDataProvider` through the constructor.

## Testing
Tests use JUnit and a fake provider that returns fixed weather data, so they never contact Open-Meteo without internet. 
They check:
- getting weather for known and unknown cities
- comparing two cities
- summary text
- the `delta` feature
- invalid commands and missing arguments
- errors coming from the provider

## Java 25
Records for the data classes, a switch statement for commands, and text for the help message.

## Additional Feature
The `Delta` command checks which city had the biggest temperature change today (the difference between the day's high and low).

## Design Reflection
1. The starter code did basically everything, but we split it up into separate classes.
2. Since it only depends on the interface now, so the data can change easily.
3. The tests pass with fixed data into a fake provider.
4. Live weather changes all the time and someone could not be connected to internet while testing.
5. Only the provider class and main would change, everything else would stay the same.

## Weather API documentation

https://open-meteo.com/en/docs
