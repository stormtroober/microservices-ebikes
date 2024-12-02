package infrastructure.adapter;

import application.ports.RestMapServiceAPI;
import domain.model.EBike;
import domain.model.EBikeFactory;
import domain.model.EBikeState;
import infrastructure.MetricsManager;
import java.util.List;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MapServiceVerticle extends AbstractVerticle {

    //private final PrometheusMeterRegistry registry;
    private final String eurekaApplicationName;
    private final String eurekaInstanceId;
    private final int port;
    private final int eurekaPort;
    private final String eurekaHost;
    private WebClient client;
    private final RestMapServiceAPI mapService;
    private final MetricsManager metricsManager;

    public MapServiceVerticle(RestMapServiceAPI mapService, String eurekaApplicationName, String eurekaInstanceId) {
        this.mapService = mapService;
        this.eurekaApplicationName = eurekaApplicationName;
        this.eurekaInstanceId = eurekaInstanceId;
        this.port = EnvUtils.getEnvOrDefaultInt("SERVICE_PORT", 8087);
        this.eurekaPort = EnvUtils.getEnvOrDefaultInt("EUREKA_PORT", 8761);
        this.eurekaHost = EnvUtils.getEnvOrDefaultString("EUREKA_HOST", "eureka-service");
        this.metricsManager = MetricsManager.getInstance();
    }

    public MapServiceVerticle(RestMapServiceAPI mapService, String eurekaApplicationName) {
        this(mapService, eurekaApplicationName, eurekaApplicationName + "-" + UUID.randomUUID().toString().substring(0, 5));
    }

    @Override
    public void start() {
        //configureMetrics();

        client = WebClient.create(vertx);
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // Enable request body handling for PUT/POST
        router.route().handler(BodyHandler.create());

        // Add Prometheus metrics endpoint
        router.get("/metrics").handler(ctx -> {
            ctx.response()
                    .putHeader("Content-Type", "text/plain")
                    .end(metricsManager.getMetrics());
        });

        router.get("/health").handler(ctx -> ctx.response().setStatusCode(200).end("OK"));

        router.route("/observeAllBikes").handler(ctx -> {
            ctx.request().toWebSocket().onComplete(webSocketAsyncResult -> {
                if (webSocketAsyncResult.succeeded()) {
                    var webSocket = webSocketAsyncResult.result();

                    // Listen to EventBus and send updates to this WebSocket
                    var consumer = vertx.eventBus().consumer("bikes.update", message -> {
                        webSocket.writeTextMessage(message.body().toString());
                    });

                    mapService.getAllBikes();

                    // Cleanup on WebSocket close
                    webSocket.closeHandler(v -> {
                        consumer.unregister();
                    });

                    webSocket.exceptionHandler(err ->  {
                        consumer.unregister();
                    });
                } else {
                    ctx.response().setStatusCode(500).end("WebSocket Upgrade Failed");
                }
            });
        });

        router.route("/observeUserBikes").handler(ctx -> {
            String username = ctx.queryParam("username").stream().findFirst().orElse(null);
            if (username == null) {
                ctx.response().setStatusCode(400).end("Missing username parameter");
                return;
            }
            ctx.request().toWebSocket().onComplete(webSocketAsyncResult -> {
                if (webSocketAsyncResult.succeeded()) {
                    var webSocket = webSocketAsyncResult.result();
                    System.out.println("User " + username + " connected");
                    // Listen to global bike updates
                    var globalConsumer = vertx.eventBus().consumer("available_bikes", message -> {
                        webSocket.writeTextMessage(message.body().toString());
                    });

                    // Listen to user-specific bike updates
                    var userConsumer = vertx.eventBus().consumer(username, message -> {
                        webSocket.writeTextMessage(message.body().toString());
                    });

                    mapService.getAllBikes(username);

                    // Cleanup on WebSocket close
                    webSocket.closeHandler(v -> {
                        ctx.response().setStatusCode(200).end("WebSocket Closed succesfully");
                        globalConsumer.unregister();
                        userConsumer.unregister();
                    });

                    webSocket.exceptionHandler(err -> {
                        ctx.response().setStatusCode(500).end("WebSocket Failed");
                        globalConsumer.unregister();
                        userConsumer.unregister();
                    });
                } else {
                    ctx.response().setStatusCode(500).end("WebSocket Failed");
                }
            });
        });


        // Start the server
        server.requestHandler(router).listen(8087, result -> {
            if (result.succeeded()) {
                System.out.println("HTTP server started on port 8087");
                registerWithEureka();
                vertx.setPeriodic(TimeUnit.SECONDS.toMillis(30), id -> sendHeartbeat());
            } else {
                System.err.println("Failed to start HTTP server: " + result.cause().getMessage());
            }
        });
    }


    private EBike createEBikeFromJson(JsonObject body) {
        String bikeName = body.getString("id");
        JsonObject location = body.getJsonObject("location");
        double x = location.getDouble("x");
        double y = location.getDouble("y");
        EBikeState state = EBikeState.valueOf(body.getString("state"));
        int batteryLevel = body.getInteger("batteryLevel");

        EBikeFactory factory = EBikeFactory.getInstance();
        return factory.createEBike(bikeName, (float) x, (float) y, state, batteryLevel);
    }

    private void configureMetrics() {
        VertxPrometheusOptions prometheusOptions = new VertxPrometheusOptions()
                .setEnabled(true)
                .setStartEmbeddedServer(false)
                .setEmbeddedServerOptions(null);

        MicrometerMetricsOptions metricsOptions = new MicrometerMetricsOptions()
                .setPrometheusOptions(prometheusOptions)
                .setEnabled(true)
                .setMicrometerRegistry(metricsManager.getRegistry());

        // Set the metrics options
        VertxOptions options = new VertxOptions().setMetricsOptions(metricsOptions);
        vertx = Vertx.vertx(options);
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