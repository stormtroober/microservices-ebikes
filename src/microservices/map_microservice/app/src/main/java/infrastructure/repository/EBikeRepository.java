package infrastructure.repository;

import domain.model.EBike;

import java.util.concurrent.CompletableFuture;

public interface EBikeRepository {
    CompletableFuture<Void> saveBike(EBike bike);

    CompletableFuture<EBike> getBike(String bikeName);

    CompletableFuture<Void> assignBikeToUser(String username, EBike bike);

    CompletableFuture<Void> unassignBikeFromUser(String username, EBike bike);
}
