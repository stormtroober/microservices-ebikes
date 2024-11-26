package infrastructure.adapter;

import application.ports.RestMapServiceAPI;
import domain.model.EBike;
import domain.model.EBikeFactory;
import domain.model.EBikeState;
import domain.model.P2d;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MicroservicesCommunication extends AbstractVerticle {

    private final RestMapServiceAPI mapService;
    private final int port;

    public MicroservicesCommunication(RestMapServiceAPI mapService) {
        this.mapService = mapService;
        this.port = EnvUtils.getEnvOrDefaultInt("COMM_MICROSERVICES_PORT", 8088);
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // Enable request body handling for PUT/POST requests
        router.route().handler(BodyHandler.create());

        // Define REST endpoint for updateEBike
        router.put("/updateEBike").handler(ctx -> {
            JsonObject body = ctx.body().asJsonObject();
            try {
                EBike bike = createEBikeFromJson(body);
                // Process the update request
                mapService.updateEBike(bike)
                        .thenAccept(v -> ctx.response().setStatusCode(200).end("EBike updated successfully"))
                        .exceptionally(ex -> {
                            ctx.response().setStatusCode(500).end("Failed to update EBike: " + ex.getMessage());
                            return null;
                        });
            } catch (Exception e) {
                ctx.response().setStatusCode(400).end("Invalid input data: " + e.getMessage());
            }
        });

        // Define REST endpoint for updateEBikes
        router.put("/updateEBikes").handler(ctx -> {
            JsonArray body = ctx.body().asJsonArray();
            try {
                body.forEach(item -> {
                    JsonObject bikeJson = (JsonObject) item;
                    EBike bike = createEBikeFromJson(bikeJson);
                    // Process the update request
                    mapService.updateEBike(bike)
                            .exceptionally(ex -> {
                                ctx.response().setStatusCode(500).end("Failed to update EBike: " + ex.getMessage());
                                return null;
                            });
                });
                ctx.response().setStatusCode(200).end("EBikes updated successfully");
            } catch (Exception e) {
                ctx.response().setStatusCode(400).end("Invalid input data: " + e.getMessage());
            }
        });

        // Start the server on the specified port
        server.requestHandler(router).listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("RestUpdateVerticle is running on port " + port);
            } else {
                System.err.println("Failed to start RestUpdateVerticle: " + result.cause().getMessage());
            }
        });
    }

    private EBike createEBikeFromJson(JsonObject body) {
        String bikeName = body.getString("id");
        double x = body.getDouble("x");
        double y = body.getDouble("y");
        EBikeState state = EBikeState.valueOf(body.getString("state"));
        int batteryLevel = body.getInteger("batteryLevel");

        EBikeFactory factory = EBikeFactory.getInstance();
        return factory.createEBike(bikeName, (float) x, (float) y, state, batteryLevel);
    }

}
