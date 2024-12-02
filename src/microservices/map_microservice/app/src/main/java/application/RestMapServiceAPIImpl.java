package application;

import application.ports.RestMapServiceAPI;

import domain.model.EBike;
import application.ports.EventPublisher;
import application.ports.EBikeRepository;
import java.util.ArrayList;
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
    public CompletableFuture<Void> updateEBikes(List<EBike> bikes) {

        return CompletableFuture.allOf(bikes.stream()
                .map(bikeRepository::saveBike)
                .toArray(CompletableFuture[]::new))
                .thenAccept(v -> {
                    //Publish the update on the global endpoint
                    var bikesInRepo = bikeRepository.getAllBikes().join();
                    eventPublisher.publishBikesUpdate(bikesInRepo);

                    List<EBike> availableBikes = bikeRepository.getAvailableBikes().join();
                    var usersWithAssignedBikes = bikeRepository.getAllUsersWithAssignedBikes().join();
                    if(!usersWithAssignedBikes.isEmpty()){
                        usersWithAssignedBikes.forEach(username -> {
                            List<EBike> userBikes = new ArrayList<>(bikesInRepo.stream()
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

                });
    }

    //TODO: make a private method for the two methods
    private void publishBikeForUser(){

    }

    @Override
    public CompletableFuture<Void> updateEBike(EBike bike) {

        return bikeRepository.saveBike(bike)
                .thenAccept(v -> {
                    var bikesInRepo = bikeRepository.getAllBikes().join();
                    eventPublisher.publishBikesUpdate(bikesInRepo);

                    List<EBike> availableBikes = bikeRepository.getAvailableBikes().join();
                    var usersWithAssignedBikes = bikeRepository.getAllUsersWithAssignedBikes().join();
                    if (!usersWithAssignedBikes.isEmpty()) {
                        usersWithAssignedBikes.forEach(username -> {
                            List<EBike> userBikes = new ArrayList<>(bikesInRepo.stream()
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
