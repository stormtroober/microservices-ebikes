package application.ports;
import io.vertx.core.json.*;

import java.util.concurrent.CompletableFuture;

public interface EbikeCommunicationPort {
    void sendUpdate(JsonObject ebike);
    CompletableFuture<JsonObject> getEbike(String id);
    void init();
}
