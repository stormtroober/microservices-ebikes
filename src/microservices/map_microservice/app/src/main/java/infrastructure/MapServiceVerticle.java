package infrastructure;

import application.ports.RestMapServiceAPI;
import domain.model.EBike;
import domain.model.EBikeState;
import domain.model.P2d;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;

public class MapServiceVerticle extends AbstractVerticle {

    private final String applicationName;
    private final String instanceId;
    private final int port;
    private WebClient client;
    private final RestMapServiceAPI mapService;

    public MapServiceVerticle(RestMapServiceAPI mapService, String applicationName, String instanceId, int port) {
        this.mapService = mapService;
        this.applicationName = applicationName;
        this.instanceId = instanceId;
        this.port = port;
    }

    public MapServiceVerticle(RestMapServiceAPI mapService, String applicationName, int port) {
        this(mapService, applicationName, "localhost:" + applicationName + ":" + port, port);
    }

    @Override
    public void start() {
        client = WebClient.create(vertx);
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // Enable request body handling for PUT/POST requests
        router.route().handler(BodyHandler.create());

        router.get("/health").handler(ctx -> ctx.response().setStatusCode(200).end("OK"));

        // Define REST endpoints
        router.put("/updateEBike").handler(ctx -> {
            JsonObject body = ctx.body().asJsonObject();
            String bikeName = body.getString("bikeName");
            double x = body.getDouble("x");
            double y = body.getDouble("y");
            EBikeState state = EBikeState.valueOf(body.getString("state"));
            int batteryLevel = body.getInteger("batteryLevel");

            mapService.updateEBike(new EBike(bikeName, new P2d(x, y), state, batteryLevel))
                    .thenAccept(v -> ctx.response().setStatusCode(200).end("OK"))
                    .exceptionally(ex -> {
                        ctx.response().setStatusCode(500).end(ex.getMessage());
                        return null;
                    });
        });

        router.post("/notifyStartRide").handler(ctx -> {
            JsonObject body = ctx.body().asJsonObject();
            String username = body.getString("username");
            String bikeName = body.getString("bikeName");

            mapService.notifyStartRide(username, bikeName)
                    .thenAccept(v -> ctx.response().setStatusCode(200).end("OK"))
                    .exceptionally(ex -> {
                        ctx.response().setStatusCode(500).end(ex.getMessage());
                        return null;
                    });
        });

        router.post("/notifyStopRide").handler(ctx -> {
            JsonObject body = ctx.body().asJsonObject();
            String username = body.getString("username");
            String bikeName = body.getString("bikeName");

            mapService.notifyStopRide(username, bikeName)
                    .thenAccept(v -> ctx.response().setStatusCode(200).end("OK"))
                    .exceptionally(ex -> {
                        ctx.response().setStatusCode(500).end(ex.getMessage());
                        return null;
                    });
        });

        router.route("/observeAllBikes").handler(ctx -> {
            ctx.request().toWebSocket().onComplete(webSocketAsyncResult -> {
                if (webSocketAsyncResult.succeeded()) {
                    var webSocket = webSocketAsyncResult.result();

                    // Listen to EventBus and send updates to this WebSocket
                    var consumer = vertx.eventBus().consumer("bikes.update", message -> {
                        webSocket.writeTextMessage(message.body().toString());
                    });

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

                    // Listen to global bike updates
                    var globalConsumer = vertx.eventBus().consumer("available_bikes", message -> {
                        webSocket.writeTextMessage(message.body().toString());
                    });

                    // Listen to user-specific bike updates
                    var userConsumer = vertx.eventBus().consumer(username, message -> {
                        webSocket.writeTextMessage(message.body().toString());
                    });

                    // Cleanup on WebSocket close
                    webSocket.closeHandler(v -> {
                        globalConsumer.unregister();
                        userConsumer.unregister();
                    });

                    webSocket.exceptionHandler(err -> {
                        globalConsumer.unregister();
                        userConsumer.unregister();
                    });
                } else {
                    ctx.response().setStatusCode(500).end("WebSocket Upgrade Failed");
                }
            });
        });


        // Start the server
        server.requestHandler(router).listen(8087, result -> {
            if (result.succeeded()) {
                System.out.println("HTTP server started on port 8087");
                //registerWithEureka();
                //vertx.setPeriodic(TimeUnit.SECONDS.toMillis(30), id -> sendHeartbeat());
            } else {
                System.err.println("Failed to start HTTP server: " + result.cause().getMessage());
            }
        });
    }

    private void registerWithEureka() {
        JsonObject instance = new JsonObject()
                .put("instance", new JsonObject()
                        .put("hostName", "map-microservice")
                        .put("app", applicationName)
                        .put("instanceId", instanceId)
                        .put("ipAddr", "127.0.0.1")
                        .put("vipAddress", applicationName)
                        .put("port", new JsonObject().put("$", port).put("@enabled", true))
                        .put("status", "UP")
                        .put("healthCheckUrl", "http://" + applicationName+ ":" + port + "/health")
                        .put("statusPageUrl", "http://" + applicationName+ ":" + port + "/info")
                        .put("homePageUrl", "http://" + applicationName+ ":" + port + "/")
                        .put("dataCenterInfo", new JsonObject()
                                .put("@class", "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo")
                                .put("name", "MyOwn")));

        client.post(8761, "eureka-server", "/eureka/apps/" + applicationName)
                .sendJsonObject(instance, res -> {
                    if (res.succeeded()) {
                        System.out.println("Successfully registered with Eureka");
                    } else {
                        System.err.println("Failed to register with Eureka: " + res.cause());
                    }
                });
    }

    private void sendHeartbeat() {
        client.put(8761, "eureka-server", "/eureka/apps/" + applicationName + "/" + instanceId)
                .send(res -> {
                    if (res.succeeded()) {
                        System.out.println("Heartbeat sent successfully");
                    } else {
                        System.err.println("Failed to send heartbeat: " + res.cause());
                    }
                });
    }
}