import application.UserServiceImpl;
import application.ports.UserEventPublisher;
import application.ports.UserServiceAPI;
import infrastructure.UserEventPublisherImpl;
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
                        "mongodb://mongodb:27017"
                ))
                .put("db_name", System.getenv().getOrDefault(
                        "MONGO_DATABSE",
                        "users_db"
                ));

        JsonObject serviceConfiguration = new JsonObject()
                .put("hostName", System.getenv().getOrDefault("SERVICE_NAME", "user-microservice"))
                .put("port", Integer.parseInt(System.getenv().getOrDefault("SERVICE_PORT", "8080")));

        MongoClient mongoClient = MongoClient.create(vertx, mongoConfig);

        MongoUserRepository repository = new MongoUserRepository(mongoClient);

        UserEventPublisher UserEventPublisher = new UserEventPublisherImpl(vertx);

        UserServiceAPI service = new UserServiceImpl(repository, UserEventPublisher);

        RESTUserAdapter controller = new RESTUserAdapter(service, vertx);

        UserVerticle userVerticle = new UserVerticle(controller, serviceConfiguration.getString("hostName"), vertx);

        userVerticle.init();

        RideCommunicationAdapter rideAdapter = new RideCommunicationAdapter(service, Integer.parseInt(System.getenv("ADAPTER_RIDE_PORT")), vertx);

        rideAdapter.init();
    }
}