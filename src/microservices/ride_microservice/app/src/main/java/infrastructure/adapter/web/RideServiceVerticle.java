package infrastructure.adapter.web;

import application.ports.RestRideServiceAPI;
import infrastructure.MetricsManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RideServiceVerticle extends AbstractVerticle {

    private final String eurekaApplicationName;
    private final String eurekaInstanceId;
    private final int port;
    private final int eurekaPort;
    private final String eurekaHost;
    private WebClient client;
    private final RestRideServiceAPI rideService;
    private final MetricsManager metricsManager;

    public RideServiceVerticle(RestRideServiceAPI rideService, String eurekaApplicationName, String eurekaInstanceId) {
        this.rideService = rideService;
        this.eurekaApplicationName = eurekaApplicationName;
        this.eurekaInstanceId = eurekaInstanceId;
        this.port = Integer.parseInt(System.getenv("SERVICE_PORT"));
        this.eurekaPort = Integer.parseInt(System.getenv("EUREKA_PORT"));;
        this.eurekaHost = System.getenv("EUREKA_HOST");
        this.metricsManager = MetricsManager.getInstance();
    }

    public RideServiceVerticle(RestRideServiceAPI rideService, String eurekaApplicationName) {
        this(rideService, eurekaApplicationName, eurekaApplicationName + "-" + UUID.randomUUID().toString().substring(0, 5));
    }

    @Override
    public void start() {
        client = WebClient.create(vertx);
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.get("/health").handler(ctx -> ctx.response().setStatusCode(200).end("OK"));
        router.get("/metrics").handler(ctx -> {
            ctx.response()
                    .putHeader("Content-Type", "text/plain")
                    .end(metricsManager.getMetrics());
        });

        router.post("/startRide").handler(ctx -> {
            metricsManager.incrementMethodCounter("startRide");
            var timer = metricsManager.startTimer();

            JsonObject body = ctx.body().asJsonObject();
            String user = body.getString("user");
            String bike = body.getString("bike");

            rideService.startRide(user, bike).thenAccept(v -> {
                ctx.response().setStatusCode(200).end("Ride started");
                metricsManager.recordTimer(timer, "startRide");
            }).exceptionally(ex -> {
                ctx.response()
                        .setStatusCode(400)  // Use 400 for client errors
                        .putHeader("Content-Type", "application/json")
                        .end(new JsonObject()
                                .put("error", ex.getMessage())
                                .encode());
                metricsManager.recordError(timer, "startRide", ex);
                return null;
            });
        });

        router.post("/stopRide").handler(ctx -> {
            metricsManager.incrementMethodCounter("stopRide");
            var timer = metricsManager.startTimer();

            JsonObject body = ctx.body().asJsonObject();
            String username = body.getString("username");

            rideService.stopRide(username).thenAccept(v -> {
                ctx.response().setStatusCode(200).end("Ride stopped");
                metricsManager.recordTimer(timer, "stopRide");
            }).exceptionally(ex -> {
                ctx.response().setStatusCode(500).end(ex.getMessage());
                metricsManager.recordError(timer, "stopRide", ex);
                return null;
            });
        });
        server.requestHandler(router).listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("HTTP server started on port " + port);
                registerWithEureka();
                vertx.setPeriodic(TimeUnit.SECONDS.toMillis(30), id -> sendHeartbeat());
            } else {
                System.err.println("Failed to start HTTP server: " + result.cause().getMessage());
            }
        });
    }

    private void registerWithEureka() {
        JsonObject instance = new JsonObject()
                .put("instance", new JsonObject()
                        .put("hostName", eurekaApplicationName)
                        .put("app", eurekaApplicationName)
                        .put("instanceId", eurekaInstanceId)
                        .put("ipAddr", "127.0.0.1")
                        .put("vipAddress", eurekaApplicationName)
                        .put("port", new JsonObject().put("$", port).put("@enabled", true))
                        .put("status", "UP")
                        .put("healthCheckUrl", "http://" + eurekaApplicationName + ":" + port + "/health")
                        .put("statusPageUrl", "http://" + eurekaApplicationName + ":" + port + "/info")
                        .put("homePageUrl", "http://" + eurekaApplicationName + ":" + port + "/")
                        .put("dataCenterInfo", new JsonObject()
                                .put("@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo")
                                .put("name", "MyOwn")));
        System.out.println("Registering with Eureka: " + instance.encodePrettily());
        System.out.println("Eureka host: " + eurekaHost + " Eureka port: " + eurekaPort);
        System.out.println("Eureka app name: " + eurekaApplicationName);

        client.post(eurekaPort, eurekaHost, "/eureka/apps/" + eurekaApplicationName)
                .sendJsonObject(instance, res -> {
                    if (res.succeeded()) {
                        System.out.println("Successfully registered with Eureka");
                    } else {
                        System.err.println("Failed to register with Eureka: " + res.cause());
                    }
                });
    }

    private void sendHeartbeat() {
        client.put(eurekaPort, eurekaHost, "/eureka/apps/" + eurekaApplicationName + "/" + eurekaInstanceId)
                .send(res -> {
                    if (res.succeeded()) {
                        System.out.println("Heartbeat sent successfully");
                    } else {
                        System.err.println("Failed to send heartbeat: " + res.cause());
                    }
                });
    }
}