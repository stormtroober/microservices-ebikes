// EBikeRepositoryImpl.java
package infrastructure;

import application.ports.EBikeRepository;
import domain.model.EBike;
import domain.model.EBikeState;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EBikeRepositoryImpl implements EBikeRepository {
    private final ConcurrentHashMap<String, EBike> bikes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> bikeAssignments = new ConcurrentHashMap<>();

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
        return CompletableFuture.runAsync(() -> {
            if (!bikes.containsKey(bike.getBikeName())) {
                throw new IllegalArgumentException("Bike not found in repository");
            }

            if (bikeAssignments.containsValue(bike.getBikeName())) {
                throw new IllegalStateException("Bike is already assigned to another user");
            }

            bikeAssignments.put(username, bike.getBikeName());
        });
    }

    @Override
    public CompletableFuture<Void> unassignBikeFromUser(String username, EBike bike) {
        return CompletableFuture.runAsync(() -> {
            if (!bikeAssignments.containsKey(username)) {
                throw new IllegalArgumentException("User does not have any bike assigned");
            }

            if (!bikeAssignments.get(username).equals(bike.getBikeName())) {
                throw new IllegalArgumentException("Bike is not assigned to the user");
            }

            bikeAssignments.remove(username);
        });
    }

    @Override
    public CompletableFuture<List<EBike>> getAvailableBikes() {
        return CompletableFuture.supplyAsync(() -> bikes.values().stream()
                .filter(bike -> bike.getState() == EBikeState.AVAILABLE)
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<String> isBikeAssigned(EBike bike) {
        return CompletableFuture.supplyAsync(() -> {
            for (Map.Entry<String, String> entry : bikeAssignments.entrySet()) {
                if (entry.getValue().equals(bike.getBikeName())) {
                    return entry.getKey();
                }
            }
            return null;
        });
    }
}