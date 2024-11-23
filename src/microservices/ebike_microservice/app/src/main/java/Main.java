import application.EBikeServiceImpl;
import infrastructure.adapters.web.EBikeVerticle;
import infrastructure.adapters.web.ServiceVerticle;
import infrastructure.persistence.config.MongoConfig;
import infrastructure.persistence.MongoEBikeRepository;
import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        // Create MongoDB client
        MongoClient mongoClient = MongoConfig.createClient(vertx);

        // Create repository
        MongoEBikeRepository repository = new MongoEBikeRepository(mongoClient);

        // Create service
        EBikeServiceImpl service = new EBikeServiceImpl(repository);

        // Deploy single verticle with both API and Eureka registration
        vertx.deployVerticle(new EBikeVerticle(service, "ebike-service", 8080));
    }
}
