package application;

import application.ports.RestMapServiceAPI;

import domain.model.EBike;
import application.ports.EventPublisher;
import application.ports.EBikeRepository;
import infrastructure.MetricsManager;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.CompletableFuture;

public class RestMapServiceAPIImpl implements RestMapServiceAPI {

    private final EBikeRepository bikeRepository;
    private final EventPublisher eventPublisher;
    private final MetricsManager metricsManager;

    public RestMapServiceAPIImpl(EBikeRepository bikeRepository, EventPublisher eventPublisher) {
        this.bikeRepository = bikeRepository;
        this.eventPublisher = eventPublisher;
        this.metricsManager = MetricsManager.getInstance();
    }

    @Override
    public CompletableFuture<Void> updateEBike(EBike bike) {
        Timer.Sample timer = metricsManager.startTimer();
        metricsManager.incrementMethodCounter("updateEBike");

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
                })
                .whenComplete((result, throwable) -> {
                    metricsManager.recordTimer(timer, "updateEBike");
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
    public void getAllBikes() {
        bikeRepository.getAllBikes().thenAccept(bikes -> {
            bikes.forEach(eventPublisher::publishBikeUpdate);
        });
    }

    @Override
    public void getAllBikesForUser(String username) {
        // Publish updates for bikes assigned to the user
        bikeRepository.getBikesAssignedToUser(username).thenAccept(userBikes -> {
            userBikes.forEach(bike -> eventPublisher.publishBikeUserUpdate(username, bike));
        });

        // Publish updates for available bikes
        bikeRepository.getAvailableBikes().thenAccept(availableBikes -> {
            availableBikes.forEach(eventPublisher::publishBikeUpdate);
        });
    }


}
