package infrastructure.adapter;

import application.ports.RestMapServiceAPI;
import domain.model.EBike;
import domain.model.EBikeFactory;
import domain.model.EBikeState;
import infrastructure.MetricsManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class RideUpdateAdapter extends AbstractVerticle {

    private final RestMapServiceAPI mapService;
    private final int port;
    private final MetricsManager metricsManager;

    public RideUpdateAdapter(RestMapServiceAPI mapService) {
        this.mapService = mapService;
        this.port = EnvUtils.getEnvOrDefaultInt("COMM_MICROSERVICES_PORT", 8089);
        this.metricsManager = MetricsManager.getInstance();
    }

    @Override
    public void start() {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // Enable request body handling for PUT/POST requests
        router.route().handler(BodyHandler.create());

        router.get("/health").handler(ctx -> ctx.response().setStatusCode(200).end("OK"));

        router.post("/notifyStartRide").handler(ctx -> {
            metricsManager.incrementMethodCounter("notifyStartRide");
            var timer = metricsManager.startTimer();

            JsonObject body = ctx.body().asJsonObject();
            String username = body.getString("username");
            String bikeName = body.getString("bikeName");

            mapService.notifyStartRide(username, bikeName)
                    .thenAccept(v -> ctx.response().setStatusCode(200).end("OK"))
                    .whenComplete((result, throwable) -> {
                        metricsManager.recordTimer(timer, "notifyStartRide");
                    })
                    .exceptionally(ex -> {
                        ctx.response().setStatusCode(500).end(ex.getMessage());
                        return null;
                    });
        });

        router.post("/notifyStopRide").handler(ctx -> {
            metricsManager.incrementMethodCounter("notifyStopRide");
            var timer = metricsManager.startTimer();

            JsonObject body = ctx.body().asJsonObject();
            String username = body.getString("username");
            String bikeName = body.getString("bikeName");

            mapService.notifyStopRide(username, bikeName)
                    .thenAccept(v -> ctx.response().setStatusCode(200).end("OK"))
                    .whenComplete((result, throwable) -> {
                        metricsManager.recordTimer(timer, "notifyStopRide");
                    })
                    .exceptionally(ex -> {
                        ctx.response().setStatusCode(500).end(ex.getMessage());
                        return null;
                    });
        });

        // Start the server on the specified port
        server.requestHandler(router).listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("RideUpdateVerticle is running on port " + port);
            } else {
                System.err.println("Failed to start RideUpdateVerticle: " + result.cause().getMessage());
            }
        });
    }

}
