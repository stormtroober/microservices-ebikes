package application.ports;

import java.util.concurrent.CompletableFuture;

public interface RestRideServiceAPI {
    CompletableFuture<Void> startRide(String userId, String bikeId);
    CompletableFuture<Void> stopRide(String userId);
}
