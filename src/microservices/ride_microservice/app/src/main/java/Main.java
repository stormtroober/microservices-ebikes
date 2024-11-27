import application.ports.RestRideServiceAPI;
import domain.model.*;
import infrastructure.adapter.RideServiceVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import application.RestRideServiceAPIImpl;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        RideRepository rideRepository = new RideRepositoryImpl(vertx);

        // Create service
        RestRideServiceAPI service = new RestRideServiceAPIImpl(rideRepository, vertx);

        // Deploy single verticle with both API and Eureka registration
        vertx.deployVerticle(new RideServiceVerticle(service, "ride-microservice"));

        // Deploy verticle for communication with other microservices
        //vertx.deployVerticle(new BikeUpdateAdapter(service));
    }
}