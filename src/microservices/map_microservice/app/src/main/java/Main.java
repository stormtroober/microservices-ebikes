import application.RestMapServiceAPIImpl;
import application.ports.EventPublisher;
import application.ports.RestMapServiceAPI;
import infrastructure.adapter.BikeUpdateAdapter;
import infrastructure.EventPublisherImpl;
import infrastructure.adapter.MapServiceVerticle;
import application.ports.EBikeRepository;
import infrastructure.EBikeRepositoryImpl;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        EBikeRepository bikeRepository = new EBikeRepositoryImpl();
        EventPublisher eventPublisher = new EventPublisherImpl(vertx);

        // Create service
        RestMapServiceAPI service = new RestMapServiceAPIImpl(bikeRepository, eventPublisher);

        // Deploy single verticle with both API and Eureka registration
        vertx.deployVerticle(new MapServiceVerticle(service, "map-microservice"));

        // Deploy verticle for communication with other microservices
        vertx.deployVerticle(new BikeUpdateAdapter(service));
    }
}