import application.RestRideServiceAPIImpl;
import application.ports.RestRideServiceAPI;
import domain.model.*;
import infrastructure.adapter.microservices_notifiers.EBikeCommunicationAdapter;
import infrastructure.adapter.microservices_notifiers.MapCommunicationAdapter;
import infrastructure.adapter.web.RideServiceVerticle;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        RideRepository rideRepository = new RideRepositoryImpl(vertx);
        String ebikeServiceUrl = "http://ebike-microservice:8082"; // Adjust the URL as needed
        String mapServiceUrl = "http://map-microservice:8088"; // Adjust the URL as needed

        EBikeCommunicationAdapter ebikeCommunicationAdapter = new EBikeCommunicationAdapter(vertx, ebikeServiceUrl);
        MapCommunicationAdapter mapCommunicationAdapter = new MapCommunicationAdapter(vertx, mapServiceUrl);

        // Create service
        RestRideServiceAPI service = new RestRideServiceAPIImpl(rideRepository, vertx, ebikeCommunicationAdapter, mapCommunicationAdapter);

        // Deploy single verticle with both API and Eureka registration
        vertx.deployVerticle(new RideServiceVerticle(service, "ride-microservice"));
    }
}