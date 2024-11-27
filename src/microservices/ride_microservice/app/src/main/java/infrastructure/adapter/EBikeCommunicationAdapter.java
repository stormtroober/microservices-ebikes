package infrastructure.adapter;

import application.ports.EbikeCommunicationPort;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.concurrent.CompletableFuture;

public class EBikeCommunicationAdapter extends AbstractVerticle implements EbikeCommunicationPort {
    private final WebClient webClient;
    private final String ebikeServiceUrl;
    private static final String RIDE_UPDATE_ADDRESS = "ride.updates";
    private final Vertx vertx;

    public EBikeCommunicationAdapter(Vertx vertx, String ebikeServiceUrl) {
        this.webClient = WebClient.create(vertx);
        this.ebikeServiceUrl = ebikeServiceUrl;
        this.vertx = vertx;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.eventBus().consumer(RIDE_UPDATE_ADDRESS, message -> {
            JsonObject ebikeUpdate = (JsonObject) message.body();
            sendUpdate(ebikeUpdate);
        });

        startPromise.complete();
    }

    public void init() {
        vertx.deployVerticle(this).onSuccess(id -> {
            System.out.println("EBikeCommunicationAdapter deployed successfully with ID: " + id);
        }).onFailure(err -> {
            System.err.println("Failed to deploy EBikeCommunicationAdapter: " + err.getMessage());
        });
    }

    @Override
    public void sendUpdate(JsonObject ebike) {
        webClient.putAbs(ebikeServiceUrl + "/api/ebikes/" + ebike.getString("id") + "/update")
                .sendJsonObject(ebike, ar -> {
                    if (ar.succeeded()) {
                        System.out.println("EBike update sent successfully");
                    } else {
                        System.err.println("Failed to send EBike update: " + ar.cause().getMessage());
                    }
                });
    }

    @Override
    public CompletableFuture<JsonObject> getEbike(String id) {
        System.out.println("Sending request to ebike-microservice -> getEbike("+id+")");
        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        webClient.getAbs(ebikeServiceUrl + "/api/ebikes/" + id)
                .send()
                .onSuccess(response -> {
                    if (response.statusCode() == 200) {
                        System.out.println("EBike received successfully");
                        future.complete(response.bodyAsJsonObject());
                    } else {
                        System.err.println("Failed to get EBike: " + response.statusCode());
                        future.complete(null);
                    }
                })
                .onFailure(err -> {
                    System.err.println("Failed to get EBike: " + err.getMessage());
                    future.complete(null);
                });

        return future;
    }
}