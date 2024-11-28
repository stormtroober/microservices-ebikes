package application;

import application.ports.EbikeCommunicationPort;
import application.ports.MapCommunicationPort;
import application.ports.RestRideServiceAPI;
import domain.model.*;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.CompletableFuture;

public class RestRideServiceAPIImpl implements RestRideServiceAPI {
    private final RideRepository rideRepository;
    private final Vertx vertx;
    private final EbikeCommunicationPort ebikeCommunicationAdapter;
    private final MapCommunicationPort mapCommunicationAdapter;

    public RestRideServiceAPIImpl(RideRepository rideRepository, Vertx vertx, EbikeCommunicationPort ebikeCommunicationAdapter, MapCommunicationPort mapCommunicationAdapter) {
        this.rideRepository = rideRepository;
        this.vertx = vertx;
        this.ebikeCommunicationAdapter = ebikeCommunicationAdapter;
        this.mapCommunicationAdapter = mapCommunicationAdapter;
        this.ebikeCommunicationAdapter.init();
        this.mapCommunicationAdapter.init();
    }

    @Override
    public CompletableFuture<Void> startRide(String userId, String bikeId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        ebikeCommunicationAdapter.getEbike(bikeId)
                .thenAccept(ebikeJson -> {
                    System.out.println("EBike: " + ebikeJson);
                    if (ebikeJson == null) {
                        System.err.println("EBike not found");
                        future.complete(null);
                        return;
                    }

                    try {
                        User user = new User(userId, User.UserType.USER, 100);
                        JsonObject location = ebikeJson.getJsonObject("location");
                        EBike ebike = new EBike(
                                ebikeJson.getString("id"),
                                location.getDouble("x"),  // Get x from location object
                                location.getDouble("y"),  // Get y from location object
                                EBikeState.valueOf(ebikeJson.getString("state")),
                                ebikeJson.getInteger("batteryLevel")
                        );
                        Ride ride = new Ride("ride-" + userId + "-" + bikeId, user, ebike);
                        rideRepository.addRide(ride);
                        rideRepository.getRideSimulation(ride.getId()).startSimulation().whenComplete((result, throwable) -> {
                            if (throwable == null) {
                                // Notify the map adapter that the ride has ended
                                mapCommunicationAdapter.notifyEndRide(bikeId, userId);
                            } else {
                                System.err.println("Error during ride simulation: " + throwable.getMessage());
                            }
                        });;
                        mapCommunicationAdapter.notifyStartRide(bikeId, userId);
                        future.complete(null);
                    } catch (Exception e) {
                        System.err.println("Error creating ride: " + e.getMessage());
                        future.completeExceptionally(e);
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Error getting EBike: " + throwable.getMessage());
                    future.completeExceptionally(throwable);
                    return null;
                });

        return future;
    }

    @Override
    public void stopRide(String userId) {
        RideSimulation rideSimulation = rideRepository.getRideSimulationByUserId(userId);
        if (rideSimulation != null) {
            rideSimulation.stopSimulationManually();
            ebikeCommunicationAdapter.sendUpdate(new JsonObject().put("id", rideSimulation.getRide().getEbike().getId()).put("state", rideSimulation.getRide().getEbike().getState().toString()));
            mapCommunicationAdapter.notifyEndRide(rideSimulation.getRide().getEbike().getId(), userId);
        }
    }
}