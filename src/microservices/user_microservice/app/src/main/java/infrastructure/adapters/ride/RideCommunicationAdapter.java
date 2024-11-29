package infrastructure.adapters.ride;

import application.UserServiceImpl;
import application.ports.UserServiceAPI;
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
    private final UserServiceAPI userService;
    private final int port;
    private final Vertx vertx;

    public RideCommunicationAdapter(UserServiceAPI userService, int port, Vertx vertx) {
        this.userService = userService;
        this.port = port;
        this.vertx = vertx;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Configure routes
        router.get("/api/users/:username").handler(this::getUser);
        router.put("/api/users/:id/update").handler(this::updateUser);

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

    private void getUser(RoutingContext ctx){
        try{
            String username = ctx.pathParam("username");
            if (username == null || username.trim().isEmpty()) {
                sendError(ctx, 400, "Invalid username");
                return;
            }
            userService.getUserByUsername(username)
                    .thenAccept(optionalUser -> {
                        if (optionalUser.isPresent()) {
                            logger.info("User found with username: " + username);
                            logger.info("Sending response to rides-microservice -> " + optionalUser.get());
                            sendResponse(ctx, 200, optionalUser.get());
                        } else {
                            logger.error("User not found with username: " + username);
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

//    private void decreaseCredit(RoutingContext ctx) {
//        try {
//            String username = ctx.pathParam("username");
//            JsonObject body = ctx.body().asJsonObject();
//            if (username == null || username.trim().isEmpty() || body == null || !body.containsKey("amount")) {
//                sendError(ctx, 400, "Invalid request");
//                return;
//            }
//
//            int amount = body.getInteger("amount");
//            userService.decreaseCredit(username, amount)
//                    .thenAccept(updatedUser -> {
//                        if (updatedUser != null) {
//                            logger.info("Credit decreased for user: " + username);
//                            sendResponse(ctx, 200, updatedUser);
//                        } else {
//                            logger.error("User not found: " + username);
//                            sendError(ctx, 404, "User not found");
//                        }
//                    })
//                    .exceptionally(e -> {
//                        handleError(ctx, e);
//                        return null;
//                    });
//        } catch (Exception e) {
//            handleError(ctx, new RuntimeException("Invalid JSON format"));
//        }
//    }

    private void updateUser(RoutingContext ctx) {
        try {
            JsonObject user = ctx.body().asJsonObject();
            userService.updateUser(user)
                    .thenAccept(updatedUser -> {
                        if (updatedUser != null) {
                            logger.info("User updated: " + updatedUser);
                            sendResponse(ctx, 200, updatedUser);
                        } else {
                            logger.error("User not found: " + user.getString("username"));
                            sendError(ctx, 404, "User not found");
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