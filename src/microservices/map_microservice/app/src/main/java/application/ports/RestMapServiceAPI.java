package application.ports;

import domain.model.EBike;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RestMapServiceAPI {
    CompletableFuture<Void> updateEBike(EBike bike);

    CompletableFuture<Void> notifyStartRide(String username, String bikeName);

    CompletableFuture<Void> notifyStopRide(String username, String bikeName);

    CompletableFuture<Void> getAllBikes();
}