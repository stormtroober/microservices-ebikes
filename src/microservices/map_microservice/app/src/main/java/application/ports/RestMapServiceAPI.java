package application.ports;

import domain.model.EBike;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RestMapServiceAPI {
    CompletableFuture<Void> updateEBike(EBike bike);

    CompletableFuture<Void> updateEBikes(List<EBike> bikes);

    CompletableFuture<Void> notifyStartRide(String username, String bikeName);

    CompletableFuture<Void> notifyStopRide(String username, String bikeName);

    void getAllBikes();

    void getAllBikes(String username);

    void registerUser(String username);
}