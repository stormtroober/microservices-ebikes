package application.ports;

import java.util.concurrent.CompletableFuture;

public interface RestRideServiceAPI {
    CompletableFuture<Void> startRide(String userId, String bikeId);
    void stopRide(String userId);
}
