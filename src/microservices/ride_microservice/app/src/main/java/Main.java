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
import infrastructure.config.ServiceConfiguration;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        ServiceConfiguration config = ServiceConfiguration.getInstance(vertx);
        config.load().onSuccess(conf -> {
            System.out.println("Configuration loaded: " + conf.encodePrettily());
            RideRepository rideRepository = new RideRepositoryImpl(vertx);
            EbikeCommunicationPort ebikeCommunicationAdapter = new EBikeCommunicationAdapter(vertx);
            MapCommunicationPort mapCommunicationAdapter = new MapCommunicationAdapter(vertx);
            UserCommunicationPort userCommunicationAdapter = new UserCommunicationAdapter(vertx);
            RestRideServiceAPI service = new RestRideServiceAPIImpl(rideRepository, vertx, ebikeCommunicationAdapter, mapCommunicationAdapter, userCommunicationAdapter);
            RideServiceVerticle rideServiceVerticle = new RideServiceVerticle(service, vertx);
            rideServiceVerticle.init();
        });






    }
}