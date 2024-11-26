package application;

import application.ports.EBikeRepository;
import application.ports.EBikeServiceAPI;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EBikeServiceImpl implements EBikeServiceAPI {

    private final EBikeRepository repository;

    public EBikeServiceImpl(EBikeRepository repository) {
        this.repository = repository;
    }

    @Override
    public CompletableFuture<JsonObject> createEBike(
            String id,
            float x,
            float y
    ) {
        JsonObject ebike = new JsonObject()
                .put("id", id)
                .put("state", "AVAILABLE")
                .put("batteryLevel", 100)
                .put("location", new JsonObject().put("x", x).put("y", y));

        return repository.save(ebike).thenApply(v -> ebike);
    }

    @Override
    public CompletableFuture<Optional<JsonObject>> getEBike(String id) {
        return repository.findById(id);
    }

    @Override
    public CompletableFuture<JsonObject> rechargeEBike(String id) {
        return repository
                .findById(id)
                .thenCompose(optionalEbike -> {
                    if (optionalEbike.isPresent()) {
                        JsonObject ebike = optionalEbike.get();
                        ebike.put("batteryLevel", 100).put("state", "AVAILABLE");
                        return repository.update(ebike).thenApply(v -> ebike);
                    }
                    return CompletableFuture.completedFuture(null);
                });
    }

    @Override
    public CompletableFuture<JsonObject> updateEBike(JsonObject ebike) {
        if (ebike.containsKey("batteryLevel")) {
            int newBattery = ebike.getInteger("batteryLevel");
            int currentBattery = ebike.getInteger("batteryLevel");
            if (newBattery < currentBattery) {
                ebike.put("batteryLevel", newBattery);
                if (newBattery == 0) {
                    ebike.put("state", "MAINTENANCE");
                }
            }
        }
        if (ebike.containsKey("state")) {
            ebike.put("state", ebike.getString("state"));
        }
        if (ebike.containsKey("location")) {
            ebike.put("location", ebike.getJsonObject("location"));
        }

        return repository.update(ebike).thenApply(v -> ebike);
    }

    @Override
    public CompletableFuture<JsonArray> getAllEBikes() {
        return repository.findAll();
    }
}