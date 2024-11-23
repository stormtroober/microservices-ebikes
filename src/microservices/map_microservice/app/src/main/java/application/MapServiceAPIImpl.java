package application;

import application.ports.MapServiceAPI;

import domain.model.EBike;
import infrastructure.EventPublisher;
import infrastructure.repository.EBikeRepository;

import java.util.concurrent.CompletableFuture;

public class MapServiceAPIImpl implements MapServiceAPI {

    private final EBikeRepository bikeRepository;
    private final EventPublisher eventPublisher;

    public MapServiceAPIImpl(EBikeRepository bikeRepository, EventPublisher eventPublisher) {
        this.bikeRepository = bikeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CompletableFuture<Void> updateEBike(EBike bike) {
        return bikeRepository.saveBike(bike)
                .thenAccept(v -> eventPublisher.publishBikeUpdate(bike));
    }


    @Override
    public CompletableFuture<Void> notifyStartRide(String username, String bikeName) {
        return bikeRepository.getBike(bikeName)
                .thenCompose(bike -> bikeRepository.assignBikeToUser(username, bike)
                        .thenAccept(v -> eventPublisher.publishBikeUpdate(bike)));
    }

    @Override
    public CompletableFuture<Void> notifyStopRide(String username, String bikeName) {
        return null;
    }

    @Override
    public CompletableFuture<Void> observeAllBikes() {
        return CompletableFuture.runAsync(eventPublisher::startBikeObservables);
    }

    @Override
    public CompletableFuture<Void> observeUserBikes(String username) {
        return null;
    }

}
