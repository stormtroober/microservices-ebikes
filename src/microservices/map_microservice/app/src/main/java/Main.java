import application.RestMapServiceAPIImpl;
import application.ports.RestMapServiceAPI;
import infrastructure.eventpublisher.EventPublisherImpl;
import infrastructure.MapServiceVerticle;
import infrastructure.repository.EBikeRepository;
import infrastructure.repository.EBikeRepositoryImpl;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        EBikeRepository bikeRepository = new EBikeRepositoryImpl();

        // Create service
        RestMapServiceAPI service = new RestMapServiceAPIImpl(bikeRepository, new EventPublisherImpl(vertx));

        // Deploy single verticle with both API and Eureka registration
        vertx.deployVerticle(new MapServiceVerticle(service, "map-microservice", 8087));
    }
}