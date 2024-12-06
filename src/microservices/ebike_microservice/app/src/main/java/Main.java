import application.EBikeServiceImpl;
import infrastructure.adapters.eureka.EurekaRegistrationAdapter;
import infrastructure.adapters.map.MapCommunicationAdapter;
import infrastructure.adapters.ride.RideCommunicationAdapter;
import infrastructure.adapters.web.EBikeVerticle;
import infrastructure.adapters.web.RESTEBikeAdapter;
import infrastructure.persistence.MongoEBikeRepository;
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
                        "mongodb://mongodb:27017"
                ))
                .put("db_name", System.getenv().getOrDefault(
                        "MONGO_DATABSE",
                        "ebikes_db"
                ));

        JsonObject eurekaConfig = new JsonObject()
                .put("eurekaHost", System.getenv().getOrDefault("EUREKA_HOST", "eureka-server"))
                .put("eurekaPort", Integer.parseInt(System.getenv().getOrDefault("EUREKA_PORT", "8761")))
                .put("eurekaEnabled", true);

        JsonObject serviceConfiguration = new JsonObject()
                .put("hostName", System.getenv().getOrDefault("SERVICE_NAME", "ebike-microservice"))
                .put("port", Integer.parseInt(System.getenv().getOrDefault("SERVICE_PORT", "8080")));
        MongoClient mongoClient = MongoClient.create(vertx, mongoConfig);

        String microserviceUrl = "http://"+System.getenv("MAP_HOST")+":"+System.getenv("MAP_PORT");
        MapCommunicationAdapter mapCommunicationAdapter = new MapCommunicationAdapter(vertx, microserviceUrl);

        MongoEBikeRepository repository = new MongoEBikeRepository(mongoClient);

        EBikeServiceImpl service = new EBikeServiceImpl(repository, mapCommunicationAdapter);

        RESTEBikeAdapter restEBikeAdapter = new RESTEBikeAdapter(service);
        RideCommunicationAdapter rideCommunicationAdapter = new RideCommunicationAdapter(service, 8081, vertx);
        rideCommunicationAdapter.init();
        EurekaRegistrationAdapter eurekaAdapter = new EurekaRegistrationAdapter(
                vertx,
                eurekaConfig
        );

        vertx.deployVerticle(new EBikeVerticle(
                restEBikeAdapter,
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