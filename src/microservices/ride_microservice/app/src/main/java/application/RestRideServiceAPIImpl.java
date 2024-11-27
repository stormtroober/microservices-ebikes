package application;

import application.ports.RestRideServiceAPI;
import domain.model.*;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;

public class RestRideServiceAPIImpl implements RestRideServiceAPI {

    private RideRepository rideRepository;
    private Vertx vertx;

    public RestRideServiceAPIImpl(RideRepository rideRepository, Vertx vertx) {
        this.rideRepository = rideRepository;
        this.vertx = vertx;
    }

    @Override
    public CompletableFuture<Void> startRide(String userId, String bikeId) {
        //Retrieve User from UserService (through adapter)
        //Retrieve Bike from BikeService (through adapter)
        //Create Ride and Start It

        //rideRepository.addRide();
        User user = new User("user1", User.UserType.USER, 100);    // 100 credits

        // Create two EBikes for the Rides
        EBike ebike1 = new EBike("ebike1", 0, 0, EBikeState.AVAILABLE, 100); // 100% battery and starting position at (0,0)
        Ride ride1 = new Ride("ride1", user, ebike1);
        rideRepository.addRide(ride1);
        CompletableFuture<Void> future = new CompletableFuture<>();
        rideRepository.getRideSimulation(ride1.getId()).startSimulation();
        future.complete(null);
        return future;
    }

    @Override
    public void stopRide(String userId) {
        RideSimulation rideSimulation = rideRepository.getRideSimulationByUserId(userId);
        rideSimulation.stopSimulationManually();
    }
}
