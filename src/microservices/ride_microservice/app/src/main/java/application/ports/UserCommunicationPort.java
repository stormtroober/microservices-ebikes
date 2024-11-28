package application.ports;

import io.vertx.core.json.JsonObject;

import java.util.concurrent.CompletableFuture;

public interface UserCommunicationPort {
    void sendUpdate(JsonObject user);
    CompletableFuture<JsonObject> getUser(String id);
    void init();
}
