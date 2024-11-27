import application.EBikeServiceImpl;
import infrastructure.adapters.eureka.EurekaRegistrationAdapter;
import infrastructure.adapters.map.MapCommunicationAdapter;
import infrastructure.adapters.ride.RideCommunicationAdapter;
import infrastructure.adapters.web.EBikeVerticle;
import infrastructure.adapters.web.RESTEBikeAdapter;
import infrastructure.config.ApplicationConfig;
import infrastructure.persistence.MongoEBikeRepository;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        ApplicationConfig config = new ApplicationConfig();

        // Create MongoDB client
        MongoClient mongoClient = MongoClient.create(vertx, config.getMongoConfig());

        // Create MapCommunicationAdapter
        String microserviceUrl = "http://map-microservice:8088"; // Adjust the URL as needed
        MapCommunicationAdapter mapCommunicationAdapter = new MapCommunicationAdapter(vertx, microserviceUrl);

        // Create repository
        MongoEBikeRepository repository = new MongoEBikeRepository(mongoClient);

        // Create service
        EBikeServiceImpl service = new EBikeServiceImpl(repository, mapCommunicationAdapter);

        // Create controllers
        RESTEBikeAdapter restEBikeAdapter = new RESTEBikeAdapter(service);
        RideCommunicationAdapter rideCommunicationAdapter = new RideCommunicationAdapter(service, 8082, vertx); // Port for RideCommunicationAdapter
        // Deploy RideCommunicationAdapter
        rideCommunicationAdapter.init();
        // Create Eureka adapter
        EurekaRegistrationAdapter eurekaAdapter = new EurekaRegistrationAdapter(
                vertx,
                config.getEurekaConfig()
        );

        // Deploy EBikeVerticle
        vertx.deployVerticle(new EBikeVerticle(
                restEBikeAdapter,
                eurekaAdapter,
                config.getHostName(),
                config.getHostName(),
                config.getPort()
        )).onSuccess(id -> {
            logger.info("EBike service started successfully on port {" + config.getPort() + "}");
            if (!config.isEurekaEnabled()) {
                logger.info("Running in standalone mode (Eureka disabled)");
            }
        }).onFailure(err -> {
            logger.error("Failed to start EBike service", err);
            vertx.close();
            System.exit(1);
        });


    }
}