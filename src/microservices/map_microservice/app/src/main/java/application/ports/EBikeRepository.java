package application.ports;

import domain.model.EBike;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EBikeRepository {
    CompletableFuture<Void> saveBike(EBike bike);

    CompletableFuture<EBike> getBike(String bikeName);

    CompletableFuture<Void> assignBikeToUser(String username, EBike bike);

    CompletableFuture<Void> unassignBikeFromUser(String username, EBike bike);

    CompletableFuture<List<EBike>> getAvailableBikes();

    CompletableFuture<String> isBikeAssigned(EBike bike);
}
