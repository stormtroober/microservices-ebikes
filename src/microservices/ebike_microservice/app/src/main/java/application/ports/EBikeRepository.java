package application.ports;

import ddd.Repository;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.CompletableFuture;

public interface EBikeRepository extends Repository {
    CompletableFuture<Void> save(JsonObject ebike);
    CompletableFuture<Void> update(JsonObject ebike);
    CompletableFuture<JsonObject> findById(String id);
    CompletableFuture<JsonArray> findAll();
}
