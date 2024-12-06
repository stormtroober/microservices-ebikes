import application.RestMapServiceAPIImpl;
import application.ports.EventPublisher;
import application.ports.RestMapServiceAPI;
import infrastructure.adapter.BikeUpdateAdapter;
import infrastructure.adapter.EventPublisherImpl;
import infrastructure.adapter.MapServiceVerticle;
import application.ports.EBikeRepository;
import infrastructure.adapter.EBikeRepositoryImpl;
import infrastructure.adapter.RideUpdateAdapter;
import infrastructure.config.ServiceConfiguration;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        ServiceConfiguration config = ServiceConfiguration.getInstance(vertx);
        config.load().onSuccess(conf -> {
            System.out.println("Configuration loaded: " + conf.encodePrettily());
            EBikeRepository bikeRepository = new EBikeRepositoryImpl();
            EventPublisher eventPublisher = new EventPublisherImpl(vertx);
            RestMapServiceAPI service = new RestMapServiceAPIImpl(bikeRepository, eventPublisher);
            MapServiceVerticle mapServiceVerticle = new MapServiceVerticle(service, vertx);
            BikeUpdateAdapter bikeUpdateAdapter = new BikeUpdateAdapter(service, vertx);
            RideUpdateAdapter rideUpdateAdapter = new RideUpdateAdapter(service, vertx);
            mapServiceVerticle.init();
            bikeUpdateAdapter.init();
            rideUpdateAdapter.init();
        });








    }
}