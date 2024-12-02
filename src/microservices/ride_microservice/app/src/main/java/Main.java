import application.RestRideServiceAPIImpl;
import application.ports.EbikeCommunicationPort;
import application.ports.MapCommunicationPort;
import application.ports.RestRideServiceAPI;
import application.ports.UserCommunicationPort;
import domain.model.*;
import infrastructure.adapter.microservices_notifiers.EBikeCommunicationAdapter;
import infrastructure.adapter.microservices_notifiers.MapCommunicationAdapter;
import infrastructure.adapter.microservices_notifiers.UserCommunicationAdapter;
import infrastructure.adapter.web.RideServiceVerticle;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        RideRepository rideRepository = new RideRepositoryImpl(vertx);
        String ebikeServiceUrl = "http://ebike-microservice:8082"; // Adjust the URL as needed
        String mapServiceUrl = "http://map-microservice:8089";
        String userServiceUrl = "http://user-microservice:8083";

        EbikeCommunicationPort ebikeCommunicationAdapter = new EBikeCommunicationAdapter(vertx, ebikeServiceUrl);
        MapCommunicationPort mapCommunicationAdapter = new MapCommunicationAdapter(vertx, mapServiceUrl);
        UserCommunicationPort userCommunicationAdapter = new UserCommunicationAdapter(vertx, userServiceUrl);

        // Create service
        RestRideServiceAPI service = new RestRideServiceAPIImpl(rideRepository, vertx, ebikeCommunicationAdapter, mapCommunicationAdapter, userCommunicationAdapter);

        // Deploy single verticle with both API and Eureka registration
        vertx.deployVerticle(new RideServiceVerticle(service, "ride-microservice"));
    }
}