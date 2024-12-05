package application.ports;

import domain.model.EBike;

import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EBikeRepository {
    CompletableFuture<Void> saveBike(EBike bike);

    CompletableFuture<EBike> getBike(String bikeName);

    CompletableFuture<Void> assignBikeToUser(String username, EBike bike);

    CompletableFuture<Void> unassignBikeFromUser(String username, EBike bike);

    CompletableFuture<List<EBike>> getAvailableBikes();

    CompletableFuture<String> isBikeAssigned(EBike bike);

    CompletableFuture<Map<String, List<EBike>>> getUsersWithAssignedAndAvailableBikes();

    CompletableFuture<List<EBike>> getAllBikes();

    CompletableFuture<List<EBike>> getAllBikes(String username);
}
