package application;

import application.ports.RestMapServiceAPI;

import domain.model.EBike;
import application.ports.EventPublisher;
import application.ports.EBikeRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RestMapServiceAPIImpl implements RestMapServiceAPI {

    private final EBikeRepository bikeRepository;
    private final EventPublisher eventPublisher;

    public RestMapServiceAPIImpl(EBikeRepository bikeRepository, EventPublisher eventPublisher) {
        this.bikeRepository = bikeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CompletableFuture<Void> updateEBike(EBike bike) {
        return bikeRepository.saveBike(bike)
                .thenAccept(v -> {
                    //Publish the update on the global endpoint
                    eventPublisher.publishBikeUpdate(bike);

                    //Publish the update on the user endpoint if the bike updated is assigned to him
                    bikeRepository.isBikeAssigned(bike).thenAccept(username -> {
                        if (username != null) {
                            eventPublisher.publishBikeUserUpdate(username, bike);
                        }
                    });

                    //Publish the update on the user endpoint for all the available bikes
                    bikeRepository.getAvailableBikes().thenAccept(availableBikes -> {
                        availableBikes.forEach(eventPublisher::publishBikeUserUpdate);
                    });
                });
    }

    @Override
    public CompletableFuture<Void> notifyStartRide(String username, String bikeName) {
        return bikeRepository.getBike(bikeName)
                .thenCompose(bike -> bikeRepository.assignBikeToUser(username, bike));
    }


    @Override
    public CompletableFuture<Void> notifyStopRide(String username, String bikeName) {
        return bikeRepository.getBike(bikeName)
                .thenCompose(bike -> bikeRepository.unassignBikeFromUser(username, bike));
    }

    @Override
    public CompletableFuture<Void> getAllBikes() {
        return bikeRepository.getAllBikes().thenAccept(bikes -> {
            bikes.forEach(eventPublisher::publishBikeUpdate);
        });
    }


}
