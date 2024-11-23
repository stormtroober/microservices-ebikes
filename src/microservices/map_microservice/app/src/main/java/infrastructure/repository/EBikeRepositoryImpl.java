package infrastructure.repository;


import domain.model.EBike;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class EBikeRepositoryImpl implements EBikeRepository {
    private final ConcurrentHashMap<String, EBike> bikes = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Void> saveBike(EBike bike) {
        bikes.put(bike.getBikeName(), bike);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<EBike> getBike(String bikeName) {
        EBike bike = bikes.get(bikeName);
        if (bike != null) {
            return CompletableFuture.completedFuture(bike);
        } else {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Bike not found"));
        }
    }

    @Override
    public CompletableFuture<Void> assignBikeToUser(String username, EBike bike) {
        // Custom logic to assign bike
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> unassignBikeFromUser(String username, EBike bike) {
        // Custom logic to unassign bike
        return CompletableFuture.completedFuture(null);
    }
}
