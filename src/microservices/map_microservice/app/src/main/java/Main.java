import application.MapServiceAPIImpl;
import application.ports.MapServiceAPI;
import infrastructure.EventPublisherImpl;
import infrastructure.MapServiceVerticle;
import infrastructure.repository.EBikeRepository;
import infrastructure.repository.EBikeRepositoryImpl;
import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        EBikeRepository bikeRepository = new EBikeRepositoryImpl();

        // Create service
        MapServiceAPI service = new MapServiceAPIImpl(bikeRepository, new EventPublisherImpl(vertx));

        // Deploy single verticle with both API and Eureka registration
        vertx.deployVerticle(new MapServiceVerticle(service, "map-microservice", 8087));
    }
}
