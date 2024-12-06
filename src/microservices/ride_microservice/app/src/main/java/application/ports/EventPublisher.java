package application.ports;

public interface EventPublisher {
    String RIDE_UPDATE_ADDRESS_EBIKE = "ride.updates.ebike";
    String RIDE_UPDATE_ADDRESS_USER = "ride.updates.user";

    void publishEBikeUpdate(String id, double x, double y, String state, int batteryLevel);
    void publishUserUpdate(String username, int credit);
}
