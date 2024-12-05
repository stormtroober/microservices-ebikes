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
        String ebikeServiceUrl = "http://"+System.getenv("EBIKE_HOST")+":"+System.getenv("EBIKE_PORT");
        String mapServiceUrl = "http://"+System.getenv("MAP_HOST")+":"+System.getenv("MAP_PORT");
        String userServiceUrl = "http://"+System.getenv("USER_HOST")+":"+System.getenv("USER_PORT");

        EbikeCommunicationPort ebikeCommunicationAdapter = new EBikeCommunicationAdapter(vertx, ebikeServiceUrl);
        MapCommunicationPort mapCommunicationAdapter = new MapCommunicationAdapter(vertx, mapServiceUrl);
        UserCommunicationPort userCommunicationAdapter = new UserCommunicationAdapter(vertx, userServiceUrl);

        RestRideServiceAPI service = new RestRideServiceAPIImpl(rideRepository, vertx, ebikeCommunicationAdapter, mapCommunicationAdapter, userCommunicationAdapter);

        vertx.deployVerticle(new RideServiceVerticle(service, System.getenv("SERVICE_NAME")));
    }
}