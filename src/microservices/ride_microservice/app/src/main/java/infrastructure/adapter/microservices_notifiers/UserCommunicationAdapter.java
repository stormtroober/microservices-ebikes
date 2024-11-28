package infrastructure.adapter.microservices_notifiers;

import application.ports.UserCommunicationPort;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.concurrent.CompletableFuture;

public class UserCommunicationAdapter extends AbstractVerticle implements UserCommunicationPort {
    private final WebClient webClient;
    private final String userServiceUrl;
    private static final String RIDE_UPDATE_ADDRESS = "ride.updates.user";
    private final Vertx vertx;

    public UserCommunicationAdapter(Vertx vertx, String userServiceUrl) {
        this.webClient = WebClient.create(vertx);
        this.userServiceUrl = userServiceUrl;
        this.vertx = vertx;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.eventBus().consumer(RIDE_UPDATE_ADDRESS, message -> {
            if (message.body() instanceof JsonObject) {
                JsonObject update = (JsonObject) message.body();
                if (update.containsKey("id")) {
                    sendUpdate(update);
                }
            }
        });

        startPromise.complete();
    }

    public void init() {
        vertx.deployVerticle(this).onSuccess(id -> {
            System.out.println("UserCommunicationAdapter deployed successfully with ID: " + id);
        }).onFailure(err -> {
            System.err.println("Failed to deploy UserCommunicationAdapter: " + err.getMessage());
        });
    }

    @Override
    public void sendUpdate(JsonObject user) {
        webClient.putAbs(userServiceUrl + "/api/users/" + user.getString("id") + "/update")
                .sendJsonObject(user, ar -> {
                    if (ar.succeeded()) {
                        System.out.println("User update sent successfully");
                    } else {
                        System.err.println("Failed to send User update: " + ar.cause().getMessage());
                    }
                });
    }

    @Override
    public CompletableFuture<JsonObject> getUser(String id) {
        System.out.println("Sending request to user-microservice -> getUser(" + id + ")");
        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        webClient.getAbs(userServiceUrl + "/api/users/" + id)
                .send()
                .onSuccess(response -> {
                    if (response.statusCode() == 200) {
                        System.out.println("User received successfully");
                        future.complete(response.bodyAsJsonObject());
                    } else {
                        System.err.println("Failed to get User: " + response.statusCode());
                        future.completeExceptionally(new RuntimeException("Failed to get User: " + response.statusCode()));
                    }
                })
                .onFailure(err -> {
                    System.err.println("Failed to get User: " + err.getMessage());
                    future.completeExceptionally(err);
                });

        return future;
    }
}