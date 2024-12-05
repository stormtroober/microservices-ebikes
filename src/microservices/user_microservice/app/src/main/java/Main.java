import application.UserServiceImpl;
import application.ports.UserEventPublisher;
import application.ports.UserServiceAPI;
import infrastructure.UserEventPublisherImpl;
import infrastructure.adapters.eureka.EurekaRegistrationAdapter;
import infrastructure.adapters.ride.RideCommunicationAdapter;
import infrastructure.adapters.web.RESTUserAdapter;
import infrastructure.adapters.web.UserVerticle;
import infrastructure.persistence.MongoUserRepository;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        JsonObject mongoConfig = new JsonObject()
                .put("connection_string", System.getenv().getOrDefault(
                        "MONGO_CONNECTION",
                        "mongodb://mongodb:27017"  // Use Docker service name
                ))
                .put("db_name", System.getenv().getOrDefault(
                        "MONGO_DATABSE",
                        "users_db"  // Use Docker service name
                ));

        JsonObject eurekaConfig = new JsonObject()
                .put("eurekaHost", System.getenv().getOrDefault("EUREKA_HOST", "eureka-server"))
                .put("eurekaPort", Integer.parseInt(System.getenv().getOrDefault("EUREKA_PORT", "8761")))
                .put("eurekaEnabled", true);

        JsonObject serviceConfiguration = new JsonObject()
                .put("hostName", System.getenv().getOrDefault("SERVICE_NAME", "user-microservice"))
                .put("port", Integer.parseInt(System.getenv().getOrDefault("SERVICE_PORT", "8080"))); // Create MongoDB client
        // Create MongoDB client
        MongoClient mongoClient = MongoClient.create(vertx, mongoConfig);

        // Create repository
        MongoUserRepository repository = new MongoUserRepository(mongoClient);

        // Create event publisher
        UserEventPublisher UserEventPublisher = new UserEventPublisherImpl(vertx);

        // Create service
        UserServiceAPI service = new UserServiceImpl(repository, UserEventPublisher);

        // Create controller
        RESTUserAdapter controller = new RESTUserAdapter(service, vertx);

        // Create Eureka adapter
        EurekaRegistrationAdapter eurekaAdapter = new EurekaRegistrationAdapter(
                vertx,
                eurekaConfig
        );

        RideCommunicationAdapter rideAdapter = new RideCommunicationAdapter(service, Integer.parseInt(System.getenv("ADAPTER_RIDE_PORT")), vertx);
        rideAdapter.init();
        // Deploy verticle
        vertx.deployVerticle(new UserVerticle(
                controller,
                eurekaAdapter,
                serviceConfiguration
        )).onSuccess(id -> {
            logger.info("EBike service started successfully on port {" + serviceConfiguration.getInteger("port") + "}");

        }).onFailure(err -> {
            logger.error("Failed to start EBike service", err);
            vertx.close();
            System.exit(1);
        });
    }
}