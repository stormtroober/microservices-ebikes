package application.ports;

import domain.model.EBike;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EBikeServiceAPI {
    CompletableFuture<JsonObject> createEBike(String id, float x, float y);
    CompletableFuture<JsonObject> getEBike(String id);
    CompletableFuture<JsonObject> rechargeEBike(String id);
    CompletableFuture<JsonObject> updateEBike(JsonObject ebike);
    CompletableFuture<JsonArray> getAllEBikes();
}