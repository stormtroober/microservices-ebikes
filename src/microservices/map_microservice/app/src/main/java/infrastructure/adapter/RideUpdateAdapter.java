package infrastructure.adapter;

import application.ports.RestMapServiceAPI;
import domain.model.EBike;
import domain.model.EBikeFactory;
import domain.model.EBikeState;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class RideUpdateAdapter extends AbstractVerticle {

    private final RestMapServiceAPI mapService;
    private final int port;

    public RideUpdateAdapter(RestMapServiceAPI mapService) {
        this.mapService = mapService;
        this.port = EnvUtils.getEnvOrDefaultInt("COMM_MICROSERVICES_PORT", 8089);
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // Enable request body handling for PUT/POST requests
        router.route().handler(BodyHandler.create());

        router.get("/health").handler(ctx -> ctx.response().setStatusCode(200).end("OK"));

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
