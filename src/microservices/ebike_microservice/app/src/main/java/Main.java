import application.EBikeServiceImpl;
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
                        "mongodb://mongodb:27017"  // Use Docker service name
                ))
                .put("db_name", System.getenv().getOrDefault(
                        "MONGO_DATABSE",
                        "ebikes_db"  // Use Docker service name
                ));

        JsonObject eurekaConfig = new JsonObject()
                .put("eurekaHost", System.getenv().getOrDefault("EUREKA_HOST", "eureka-server"))
                .put("eurekaPort", Integer.parseInt(System.getenv().getOrDefault("EUREKA_PORT", "8761")))
                .put("eurekaEnabled", true);

        JsonObject serviceConfiguration = new JsonObject()
                .put("hostName", System.getenv().getOrDefault("SERVICE_NAME", "ebike-microservice"))
                .put("port", Integer.parseInt(System.getenv().getOrDefault("SERVICE_PORT", "8080")));
        MongoClient mongoClient = MongoClient.create(vertx, mongoConfig);

        // Create MapCommunicationAdapter
        String microserviceUrl = "http://"+System.getenv("MAP_HOST")+":"+System.getenv("MAP_PORT"); // Adjust the URL as needed
        MapCommunicationAdapter mapCommunicationAdapter = new MapCommunicationAdapter(vertx, microserviceUrl);

        // Create repository
        MongoEBikeRepository repository = new MongoEBikeRepository(mongoClient);

        // Create service
        EBikeServiceImpl service = new EBikeServiceImpl(repository, mapCommunicationAdapter);

        // Create controllers
        RESTEBikeAdapter restEBikeAdapter = new RESTEBikeAdapter(service);
        RideCommunicationAdapter rideCommunicationAdapter = new RideCommunicationAdapter(service, 8081, vertx); // Port for RideCommunicationAdapter
        // Deploy RideCommunicationAdapter
        rideCommunicationAdapter.init();
        // Create Eureka adapter
        EBikeVerticle eBikeVerticle = new EBikeVerticle(restEBikeAdapter, serviceConfiguration, vertx);
        eBikeVerticle.init();




    }
}