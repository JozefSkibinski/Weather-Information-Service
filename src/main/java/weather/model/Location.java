package weather.model;

public record Location(String name, double latitude, double longitude){
    public Location{
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("Name is null or empty");
        }
        if(latitude < -90 || latitude > 90){
            throw new IllegalArgumentException("Latitude is out of range");
        }
        if(longitude < -180 || longitude > 180){
            throw new IllegalArgumentException("Longitude is out of range");
        }
    }
}

