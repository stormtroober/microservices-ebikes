import application.RestRideServiceAPIImpl;
import application.ports.RestRideServiceAPI;
import domain.model.*;
import infrastructure.adapter.EBikeCommunicationAdapter;
import infrastructure.adapter.RideServiceVerticle;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        RideRepository rideRepository = new RideRepositoryImpl(vertx);
        String ebikeServiceUrl = "http://ebike-microservice:8082"; // Adjust the URL as needed
        EBikeCommunicationAdapter ebikeCommunicationAdapter = new EBikeCommunicationAdapter(vertx, ebikeServiceUrl);



        // Create service
        RestRideServiceAPI service = new RestRideServiceAPIImpl(rideRepository, vertx, ebikeCommunicationAdapter);

        // Deploy single verticle with both API and Eureka registration
        vertx.deployVerticle(new RideServiceVerticle(service, "ride-microservice"));
    }
}