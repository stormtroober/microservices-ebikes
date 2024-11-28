import application.UserServiceImpl;
import application.ports.UserServiceAPI;
import infrastructure.adapters.eureka.EurekaRegistrationAdapter;
import infrastructure.adapters.web.RESTUserAdapter;
import infrastructure.adapters.web.UserVerticle;
import infrastructure.config.ApplicationConfig;
import infrastructure.persistence.MongoUserRepository;
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

        // Create repository
        MongoUserRepository repository = new MongoUserRepository(mongoClient);

        // Create service
        UserServiceAPI service = new UserServiceImpl(repository);

        // Create controller
        RESTUserAdapter controller = new RESTUserAdapter(service, vertx);

        // Create Eureka adapter
        EurekaRegistrationAdapter eurekaAdapter = new EurekaRegistrationAdapter(
                vertx,
                config.getEurekaConfig()
        );

        // Deploy verticle
        vertx.deployVerticle(new UserVerticle(
                controller,
                eurekaAdapter,
                config.getHostName(),
                config.getHostName(),
                config.getPort()
        )).onSuccess(id -> {
            logger.info("User service started successfully on port {" + config.getPort() + "}");
            if (!config.isEurekaEnabled()) {
                logger.info("Running in standalone mode (Eureka disabled)");
            }
        }).onFailure(err -> {
            logger.error("Failed to start User service", err);
            vertx.close();
            System.exit(1);
        });
    }
}