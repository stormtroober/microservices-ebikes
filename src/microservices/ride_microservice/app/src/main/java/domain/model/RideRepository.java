package domain.model;

public interface RideRepository {
    void addRide(Ride ride);
    void removeRide(Ride ride);
    Ride getRide(String rideId);
    //void updateRide(Ride ride);
    RideSimulation getRideSimulation(String rideId);
    RideSimulation getRideSimulationByUserId(String userId);
}
