package infrastructure;

import application.ports.RestMapServiceAPI;
import domain.model.EBike;
import domain.model.EBikeState;
import domain.model.P2d;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MicroservicesCommunication extends AbstractVerticle {

    private final RestMapServiceAPI mapService;
    private final int port;

    public MicroservicesCommunication(RestMapServiceAPI mapService, int port) {
        this.mapService = mapService;
        this.port = port;
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
                String bikeName = body.getString("bikeName");
                double x = body.getDouble("x");
                double y = body.getDouble("y");
                EBikeState state = EBikeState.valueOf(body.getString("state"));
                int batteryLevel = body.getInteger("batteryLevel");

                // Process the update request
                mapService.updateEBike(new EBike(bikeName, new P2d(x, y), state, batteryLevel))
                        .thenAccept(v -> ctx.response().setStatusCode(200).end("EBike updated successfully"))
                        .exceptionally(ex -> {
                            ctx.response().setStatusCode(500).end("Failed to update EBike: " + ex.getMessage());
                            return null;
                        });
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

}
