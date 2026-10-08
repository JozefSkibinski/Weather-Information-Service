package weather.cli;

import weather.model.Location;
import weather.model.WeatherData;
import weather.provider.WeatherDataProvider.WeatherProviderException;
import weather.service.WeatherService;
import weather.service.WeatherService.Comparison;
import weather.service.WeatherService.UnknownLocationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WeatherCLI{


    private final WeatherService service;

    public WeatherCLI(WeatherService service){
        this.service = service;
    }

    public record CommandResult(String output, boolean exit){ }


    private static final String HELP = """
            Available commands:
              help                            Show this help message
              locations                       List the known locations
              current <location>              Current weather for a location
              compare <location> <location>   Compare two locations (quote multi-word names)
              summary <location>              A short description of current conditions
              quit                            Exit the application
              delta                           City with the biggest temperature change today
            Example: compare Chicago "New York\"""";




    public void run(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Weather Information Service — type 'help' for commands.");

        while(true){
            System.out.print("weather> ");
            if (!scanner.hasNextLine()){
                System.out.println();
                System.out.println("Goodbye!");
                break;
            }

            String line = scanner.nextLine();
            CommandResult result = execute(line);
            if(!result.output().isEmpty()){
                System.out.println(result.output());
            }
            if(result.exit()){
                break;
            }
        }
    }


    public CommandResult execute(String line){
        List<String> tokens;
        try{
            tokens = tokenize(line);
        } catch(IllegalArgumentException e){
            return new CommandResult("Error: "+e.getMessage(), false);
        }
        if(tokens.isEmpty()){
            return new CommandResult("", false);   // blank line → do nothing
        }

        String command = tokens.get(0).toLowerCase();
        List<String> args = tokens.subList(1, tokens.size());

        try{
            return switch(command){
                case "help" -> new CommandResult(HELP, false);
                case "locations" -> new CommandResult(formatLocations(), false);
                case "current"   -> handleCurrent(args);
                case "compare"   -> handleCompare(args);
                case "summary"   -> handleSummary(args);
                case "delta"     -> new CommandResult(service.biggestTemperatureChange(), false);
                case "quit"      -> new CommandResult("Goodbye!", true);
                default -> new CommandResult(
                        "Unknown command: '" + tokens.get(0) + "'. Type 'help' to see available commands.", false);
            };
        }catch(UnknownLocationException e){
            return new CommandResult(e.getMessage() + ". " + formatLocations(), false);
        }catch(WeatherProviderException e){
            return new CommandResult("Weather data unavailable: " + e.getMessage(), false);
        }
    }

    private CommandResult handleCurrent(List<String> args) throws UnknownLocationException, WeatherProviderException{
        if(args.isEmpty()){
            return new CommandResult("Usage: current <location>",false);
        }
        String name = String.join(" ", args);
        WeatherData data = service.getCurrentWeather(name);
        return new CommandResult(formatCurrent(data), false);
    }

    private CommandResult handleCompare(List<String> args) throws UnknownLocationException, WeatherProviderException{

        if(args.size() != 2){
            return new CommandResult("Usage: compare <location> <location> (put multi-word names in quotes)",false);
        }
        Comparison c = service.compare(args.get(0), args.get(1));
        return new CommandResult(formatComparison(c), false);
    }

    private CommandResult handleSummary(List<String> args) throws UnknownLocationException, WeatherProviderException{
        if(args.isEmpty()){
            return new CommandResult("Usage: summary <location>", false);
        }
        String name= String.join(" ", args);
        String summary =service.summarize(name);
        return new CommandResult(summary,false);
    }



    private String formatLocations(){
        List<String> names = new ArrayList<>();

        for (Location location : service.getLocations()){
            names.add(location.name());
        }
        return "Known locations: " + String.join(", ", names);
    }

    private static String formatCurrent(WeatherData data){
        return String.format(
                "%s%n" +"  Temperature:     %.1f°F%n" + "  Chance of rain:  %d%%%n" +"  Wind speed:      %.1f mph",
                data.location().name(),
                data.temperatureF(),
                data.precipitationProbabilityPercent(),
                data.windSpeedMph());
    }

    private static String formatComparison(Comparison c){
        WeatherData one = c.first();
        WeatherData two = c.second();
        String row = "%-16s%-15s%-15s%s%n";

        return String.format(row, "", one.location().name(), two.location().name(), "Difference")
                + String.format(row, "Temperature",
                String.format("%.1f°F", one.temperatureF()),
                String.format("%.1f°F", two.temperatureF()),
                String.format("%+.1f°F", c.temperatureDifference()))
                + String.format(row, "Chance of rain",
                one.precipitationProbabilityPercent() + "%",
                two.precipitationProbabilityPercent() + "%",
                String.format("%+d%%", c.precipitationDifference()))
                + String.format(row, "Wind speed",
                String.format("%.1f mph", one.windSpeedMph()),
                String.format("%.1f mph", two.windSpeedMph()),
                String.format("%+.1f mph", c.windSpeedDifference()))
                + "Warmer: " + c.warmerCity();
    }


    static List<String> tokenize(String line){
        List<String> tokens = new ArrayList<>();
        if (line == null){
            return tokens;
        }
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for(char c : line.toCharArray()){
            if(c == '"'){
                inQuotes = !inQuotes;
            }else if(Character.isWhitespace(c) && !inQuotes){
                if(current.length() > 0){
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            }else{
                current.append(c);
            }

        }

        if(inQuotes){
            throw new IllegalArgumentException("missing closing quote");
        }
        if(current.length() > 0){
            tokens.add(current.toString());
        }


        return tokens;
    }
}
