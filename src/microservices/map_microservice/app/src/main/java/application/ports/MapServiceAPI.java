package application.ports;

import domain.model.EBike;
import io.vertx.core.Future;

import java.util.concurrent.CompletableFuture;

public interface MapServiceAPI {
    CompletableFuture<Void> updateEBike(EBike bike);

    CompletableFuture<Void> notifyStartRide(String username, String bikeName);

    CompletableFuture<Void> notifyStopRide(String username, String bikeName);

    CompletableFuture<Void> observeAllBikes();

    CompletableFuture<Void> observeUserBikes(String username);
}