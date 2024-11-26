package infrastructure.adapters.web;

import application.ports.EBikeServiceAPI;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RESTEBikeAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RESTEBikeAdapter.class);
    private final EBikeServiceAPI ebikeService;

    public RESTEBikeAdapter(EBikeServiceAPI ebikeService) {
        this.ebikeService = ebikeService;
    }

    public void configureRoutes(Router router) {
        router.post("/api/ebikes/create").handler(this::createEBike);
        router.get("/api/ebikes/:id").handler(this::getEBike);
        router.put("/api/ebikes/:id/recharge").handler(this::rechargeEBike);
        router.put("/api/ebikes/:id/update").handler(this::updateEBike);
        router.get("/api/ebikes").handler(this::getAllEBikes);
        router.get("/health").handler(this::healthCheck);
    }

    private void createEBike(RoutingContext ctx) {
        try {
            JsonObject body = ctx.body().asJsonObject();
            String id = body.getString("id");
            float x = body.getFloat("x", 0.0f);
            float y = body.getFloat("y", 0.0f);

            if (id == null || id.trim().isEmpty()) {
                sendError(ctx, 400, "Invalid id");
                return;
            }

            ebikeService.createEBike(id, x, y)
                    .thenAccept(result -> sendResponse(ctx, 201, result))
                    .exceptionally(e -> {
                        handleError(ctx, e);
                        return null;
                    });
        } catch (Exception e) {
            handleError(ctx, new RuntimeException("Invalid JSON format"));
        }
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

    private void rechargeEBike(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        if (id == null || id.trim().isEmpty()) {
            sendError(ctx, 400, "Invalid id");
            return;
        }

        ebikeService.rechargeEBike(id)
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

    private void getAllEBikes(RoutingContext ctx) {
        ebikeService.getAllEBikes()
                .thenAccept(result -> sendResponse(ctx, 200, result))
                .exceptionally(e -> {
                    handleError(ctx, e);
                    return null;
                });
    }

    private void healthCheck(RoutingContext ctx) {
        JsonObject health = new JsonObject()
                .put("status", "UP")
                .put("timestamp", System.currentTimeMillis());
        sendResponse(ctx, 200, health);
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
