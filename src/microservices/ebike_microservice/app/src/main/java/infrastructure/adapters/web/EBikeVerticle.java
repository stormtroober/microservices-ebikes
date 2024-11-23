package infrastructure.adapters.web;

import application.ports.EBikeServiceAPI;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.concurrent.TimeUnit;

public class EBikeVerticle extends AbstractVerticle {

    private final EBikeServiceAPI ebikeService;
    private final String applicationName;
    private final int port;
    private WebClient client;

    public EBikeVerticle(
        EBikeServiceAPI ebikeService,
        String applicationName,
        int port
    ) {
        this.ebikeService = ebikeService;
        this.applicationName = applicationName;
        this.port = port;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        client = WebClient.create(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Health and info endpoints for Eureka
        router
            .get("/health")
            .handler(ctx ->
                ctx
                    .response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("status", "UP").encode())
            );

        router
            .get("/info")
            .handler(ctx ->
                ctx
                    .response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("info", "EBike Service").encode())
            );

        // API endpoints
        router.post("/api/ebikes").handler(this::createEBike);
        router.get("/api/ebikes/:id").handler(this::getEBike);
        router.put("/api/ebikes/:id/recharge").handler(this::rechargeEBike);
        router.put("/api/ebikes/:id").handler(this::updateEBike);
        router.get("/api/ebikes").handler(this::getAllEBikes);

        // Start the HTTP server
        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(port)
            .onSuccess(server -> {
                System.out.println("HTTP server started on port " + port);
                registerWithEureka();
                vertx.setPeriodic(TimeUnit.SECONDS.toMillis(30), id ->
                    sendHeartbeat()
                );
                startPromise.complete();
            })
            .onFailure(startPromise::fail);
    }

    private void registerWithEureka() {
        String instanceId = applicationName + ":" + port;
        JsonObject instance = new JsonObject()
            .put(
                "instance",
                new JsonObject()
                    .put("hostName", "ebike-service")
                    .put("app", applicationName.toUpperCase())
                    .put("instanceId", instanceId)
                    .put("ipAddr", "ebike-service")
                    .put("vipAddress", applicationName.toLowerCase())
                    .put("status", "UP")
                    .put(
                        "port",
                        new JsonObject().put("$", port).put("@enabled", true)
                    )
                    .put(
                        "healthCheckUrl",
                        "http://ebike-service:" + port + "/health"
                    )
                    .put(
                        "statusPageUrl",
                        "http://ebike-service:" + port + "/info"
                    )
                    .put("homePageUrl", "http://ebike-service:" + port + "/")
                    .put(
                        "dataCenterInfo",
                        new JsonObject()
                            .put(
                                "@class",
                                "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo"
                            )
                            .put("name", "MyOwn")
                    )
            );

        client
            .post(
                8761,
                "eureka-server",
                "/eureka/apps/" + applicationName.toUpperCase()
            )
            .sendJsonObject(instance)
            .onSuccess(response -> System.out.println("Registered with Eureka"))
            .onFailure(err -> System.err.println("Failed to register: " + err));
    }

    private void sendHeartbeat() {
        String instanceId = applicationName + ":" + port;
        client
            .put(
                8761,
                "eureka-server",
                "/eureka/apps/" +
                applicationName.toUpperCase() +
                "/" +
                instanceId
            )
            .send()
            .onSuccess(response -> System.out.println("Heartbeat sent"))
            .onFailure(err -> System.err.println("Heartbeat failed: " + err));
    }

    private void createEBike(RoutingContext ctx) {
        System.out.println("RECEIVED POST");
        JsonObject body = ctx.getBodyAsJson();
        String id = body.getString("id");
        float x = body.getFloat("x");
        float y = body.getFloat("y");
        System.out.println("-------------->"+ id);
        ebikeService
            .createEBike(id, x, y)
            .thenAccept(result -> sendResponse(ctx, 201, result))
            .exceptionally(e -> {
                handleError(ctx, e);
                return null;
            });
    }

    private void getEBike(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        ebikeService
            .getEBike(id)
            .thenAccept(result -> {
                if (result != null) {
                    sendResponse(ctx, 200, result);
                } else {
                    ctx.response().setStatusCode(404).end();
                }
            })
            .exceptionally(e -> {
                handleError(ctx, e);
                return null;
            });
    }

    private void rechargeEBike(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        ebikeService
            .rechargeEBike(id)
            .thenAccept(result -> {
                if (result != null) {
                    sendResponse(ctx, 200, result);
                } else {
                    ctx.response().setStatusCode(404).end();
                }
            })
            .exceptionally(e -> {
                handleError(ctx, e);
                return null;
            });
    }

    private void updateEBike(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        JsonObject body = ctx.getBodyAsJson();
        body.put("id", id);

        ebikeService
            .updateEBike(body)
            .thenAccept(result -> {
                if (result != null) {
                    sendResponse(ctx, 200, result);
                } else {
                    ctx.response().setStatusCode(404).end();
                }
            })
            .exceptionally(e -> {
                handleError(ctx, e);
                return null;
            });
    }

    private void getAllEBikes(RoutingContext ctx) {
        ebikeService
            .getAllEBikes()
            .thenAccept(result -> sendResponse(ctx, 200, result))
            .exceptionally(e -> {
                handleError(ctx, e);
                return null;
            });
    }

    private void sendResponse(
        RoutingContext ctx,
        int statusCode,
        Object result
    ) {
        HttpServerResponse response = ctx.response();
        response
            .setStatusCode(statusCode)
            .putHeader("content-type", "application/json")
            .end(result.toString());
    }

    private void handleError(RoutingContext ctx, Throwable e) {
        ctx
            .response()
            .setStatusCode(500)
            .putHeader("content-type", "application/json")
            .end(new JsonObject().put("error", e.getMessage()).encode());
    }
}
