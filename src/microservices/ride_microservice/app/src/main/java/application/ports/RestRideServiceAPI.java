package application.ports;

public interface RestRideServiceAPI {
    void startRide(String userId, String bikeId);
    void stopRide(String userId);
}
