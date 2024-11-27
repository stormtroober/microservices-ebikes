package infrastructure.adapters.ride;

import application.ports.EBikeServiceAPI;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RideCommunicationAdapter extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(RideCommunicationAdapter.class);
    private final EBikeServiceAPI ebikeService;
    private final int port;
    private final Vertx vertx;

    public RideCommunicationAdapter(EBikeServiceAPI ebikeService, int port, Vertx vertx) {
        this.ebikeService = ebikeService;
        this.port = port;
        this.vertx = vertx;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Configure routes
        router.get("/api/ebikes/:id").handler(this::getEBike);
        router.put("/api/ebikes/:id/update").handler(this::updateEBike);

        // Start HTTP server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port)
                .onSuccess(server -> {
                    logger.info("HTTP server started on port {}", port);
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }

    public void init() {
        vertx.deployVerticle(this).onSuccess(id -> {
            logger.info("RideCommunicationAdapter deployed successfully with ID: " + id);
        }).onFailure(err -> {
            logger.error("Failed to deploy RideCommunicationAdapter", err);
        });
    }

    private void getEBike(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        if (id == null || id.trim().isEmpty()) {
            sendError(ctx, 400, "Invalid id");
            return;
        }

        ebikeService.getEBike(id)
                .thenAccept(optionalEBike -> {
                    if (optionalEBike.isPresent()) {
                        sendResponse(ctx, 200, optionalEBike.get());
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
        try {
            JsonObject body = ctx.body().asJsonObject();
            String id = ctx.pathParam("id");
            body.put("id", id);

            ebikeService.updateEBike(body)
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
        } catch (Exception e) {
            handleError(ctx, new RuntimeException("Invalid JSON format"));
        }
    }

    private void sendResponse(RoutingContext ctx, int statusCode, Object result) {
        ctx.response()
                .setStatusCode(statusCode)
                .putHeader("content-type", "application/json")
                .end(result instanceof String ? (String) result : result.toString());
    }

    private void sendError(RoutingContext ctx, int statusCode, String message) {
        JsonObject error = new JsonObject().put("error", message);
        ctx.response()
                .setStatusCode(statusCode)
                .putHeader("content-type", "application/json")
                .end(error.encode());
    }

    private void handleError(RoutingContext ctx, Throwable e) {
        logger.error("Error processing request", e);
        ctx.response()
                .setStatusCode(500)
                .putHeader("content-type", "application/json")
                .end(new JsonObject()
                        .put("error", e.getMessage())
                        .encode());
    }
}