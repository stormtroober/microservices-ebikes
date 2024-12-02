package application;

import application.ports.RestMapServiceAPI;

import domain.model.EBike;
import application.ports.EventPublisher;
import application.ports.EBikeRepository;
import infrastructure.MetricsManager;
import io.micrometer.core.instrument.Timer;

import java.util.ArrayList;
import java.util.List;
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
    public CompletableFuture<Void> updateEBikes(List<EBike> bikes) {
        Timer.Sample timer = metricsManager.startTimer();
        metricsManager.incrementMethodCounter("updateEBikes");

        return CompletableFuture.allOf(bikes.stream()
                .map(bikeRepository::saveBike)
                .toArray(CompletableFuture[]::new))
                .thenAccept(v -> {
                    //Publish the update on the global endpoint
                    eventPublisher.publishBikesUpdate(bikes);

                    List<EBike> availableBikes = bikeRepository.getAvailableBikes().join();
                    var usersWithAssignedBikes = bikeRepository.getAllUsersWithAssignedBikes().join();
                    if(!usersWithAssignedBikes.isEmpty()){
                        usersWithAssignedBikes.forEach(username -> {
                            List<EBike> userBikes = new ArrayList<>(bikes.stream()
                                    .filter(bike -> {
                                        String assignedUser = bikeRepository.isBikeAssigned(bike).join();
                                        return assignedUser != null && assignedUser.equals(username);
                                    }).toList());
                            userBikes.addAll(availableBikes);
                            eventPublisher.publishUserBikesUpdate(userBikes, username);
                        });
                    }
                    else{
                        eventPublisher.publishUserAvailableBikesUpdate(availableBikes);
                    }

                })
                .whenComplete((result, throwable) -> {
                    metricsManager.recordTimer(timer, "updateEBikes");
                });
    }

    //TODO: make a private method for the two methods
    private void publishBikeForUser(){

    }

    @Override
    public CompletableFuture<Void> updateEBike(EBike bike) {
        Timer.Sample timer = metricsManager.startTimer();
        metricsManager.incrementMethodCounter("updateEBike");

        return bikeRepository.saveBike(bike)
                .thenAccept(v -> {
                    // Publish the update on the global endpoint
                    List<EBike> bikes = List.of(bike);
                    eventPublisher.publishBikesUpdate(bikes);

                    List<EBike> availableBikes = bikeRepository.getAvailableBikes().join();
                    var usersWithAssignedBikes = bikeRepository.getAllUsersWithAssignedBikes().join();
                    if (!usersWithAssignedBikes.isEmpty()) {
                        usersWithAssignedBikes.forEach(username -> {
                            List<EBike> userBikes = new ArrayList<>(bikes.stream()
                                    .filter(b -> {
                                        String assignedUser = bikeRepository.isBikeAssigned(b).join();
                                        return assignedUser != null && assignedUser.equals(username);
                                    }).toList());
                            userBikes.addAll(availableBikes);
                            eventPublisher.publishUserBikesUpdate(userBikes, username);
                        });
                    } else {
                        eventPublisher.publishUserAvailableBikesUpdate(availableBikes);
                    }
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
        bikeRepository.getAllBikes().thenAccept(eventPublisher::publishBikesUpdate);
    }

    @Override
    public void getAllBikes(String username) {
        List<EBike> availableBikes = bikeRepository.getAvailableBikes().join();
        List<EBike> userBikes = bikeRepository.getAllBikes(username).join();
        availableBikes.addAll(userBikes);
        eventPublisher.publishUserBikesUpdate(availableBikes, username);
    }

}
