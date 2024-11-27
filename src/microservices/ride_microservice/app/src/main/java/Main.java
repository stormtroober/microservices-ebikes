import domain.model.*;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import application.RestRideServiceAPIImpl;

public class Main {
    public static void main(String[] args) {
        // Initialize Vert.x
        Vertx vertx = Vertx.vertx();
        EventBus eventBus = vertx.eventBus();

        // Create RideRepository
        RideRepository rideRepository = new RideRepositoryImpl(vertx);

        // Create RestRideServiceAPIImpl
        RestRideServiceAPIImpl restRideServiceAPI = new RestRideServiceAPIImpl(rideRepository, vertx);

        // Start a ride
        restRideServiceAPI.startRide("user1", "ebike1");

        // Set up a consumer to listen for ride updates on the Event Bus
        eventBus.consumer("ride.updates", message -> {
            System.out.println("Received Ride Update: " + message.body());
        });

        vertx.setTimer(5000, timerId -> {
            // Stop the ride after 5 seconds
            restRideServiceAPI.stopRide("user1");
        });

        // Add a hook to stop the application when terminated
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            vertx.close();
        }));
    }
}