import application.RestRideServiceAPIImpl;
import application.ports.*;
import domain.model.*;
import infrastructure.adapter.microservices.eventbus.EventPublisherImpl;
import infrastructure.adapter.microservices.notifiers.EBikeCommunicationAdapter;
import infrastructure.adapter.microservices.notifiers.MapCommunicationAdapter;
import infrastructure.adapter.microservices.notifiers.UserCommunicationAdapter;
import infrastructure.adapter.web.RideServiceVerticle;
import infrastructure.config.ServiceConfiguration;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        ServiceConfiguration config = ServiceConfiguration.getInstance(vertx);
        config.load().onSuccess(conf -> {
            System.out.println("Configuration loaded: " + conf.encodePrettily());
            EbikeCommunicationPort ebikeCommunicationAdapter = new EBikeCommunicationAdapter(vertx);
            MapCommunicationPort mapCommunicationAdapter = new MapCommunicationAdapter(vertx);
            UserCommunicationPort userCommunicationAdapter = new UserCommunicationAdapter(vertx);
            RestRideServiceAPI service = new RestRideServiceAPIImpl(new EventPublisherImpl(vertx), vertx, ebikeCommunicationAdapter, mapCommunicationAdapter, userCommunicationAdapter);
            RideServiceVerticle rideServiceVerticle = new RideServiceVerticle(service, vertx);
            rideServiceVerticle.init();
        });






    }
}